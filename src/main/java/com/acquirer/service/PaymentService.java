package com.acquirer.service;

import com.acquirer.dto.IssuerResponse;
import com.acquirer.dto.PaymentRequest;
import com.acquirer.dto.PaymentResponse;
import com.acquirer.entity.Merchant;
import com.acquirer.entity.Transaction;
import com.acquirer.entity.Transaction.TransactionStatus;
import com.acquirer.exception.MerchantNotFoundException;
import com.acquirer.exception.ValidationException;
import com.acquirer.repository.TransactionRepository;
import com.acquirer.service.ValidationService.ValidationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final TransactionRepository transactionRepository;
    private final MerchantService merchantService;
    private final ValidationService validationService;
    private final IssuerService issuerService;

    /**
     * Process a payment request from a merchant
     * 
     * @param request Payment request details
     * @return PaymentResponse with transaction result
     */
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        String transactionId = UUID.randomUUID().toString();
        
        // Set up MDC for logging traceability
        MDC.put("transactionId", transactionId);
        MDC.put("merchantId", request.getMerchantId());

        log.info("=== STARTING PAYMENT PROCESSING ===");
        log.info("Payment request received - Merchant: {}, Amount: {} {}, Card: {}",
                request.getMerchantId(), request.getAmount(), request.getCurrency(),
                maskCardToken(request.getCardToken()));

        Transaction transaction = null;
        try {
            transaction = createInitialTransaction(transactionId, request);
            transaction = transactionRepository.save(transaction);
            log.debug("Transaction created with PENDING status");

            log.info("Validating merchant");
            Merchant merchant = validateMerchant(request.getMerchantId());
            log.info("Merchant validation PASSED - Merchant: {} is active", merchant.getMerchantName());

            log.info("Validating business rules");
            validateBusinessRules(request, merchant);
            log.info("Business rules validation PASSED");

            log.info("Requesting authorization from issuer");
            IssuerResponse issuerResponse = issuerService.authorizeTransaction(
                    request.getCardToken(),
                    request.getAmount(),
                    request.getCurrency()
            );

            log.info("Processing issuer response");
            transaction = updateTransactionWithIssuerResponse(transaction, issuerResponse);
            transaction = transactionRepository.save(transaction);

            log.info("=== PAYMENT PROCESSING COMPLETED - Status: {} ===", transaction.getStatus());

            return buildPaymentResponse(transaction);

        } catch (ValidationException | MerchantNotFoundException e) {
            log.error("Payment validation failed: {}", e.getMessage());
            if (transaction != null) {
                transaction = handleTransactionError(transaction, e.getMessage());
                transaction = transactionRepository.save(transaction);
            }
            throw e;

        } catch (Exception e) {
            log.error("Unexpected error processing payment", e);
            if (transaction != null) {
                transaction = handleTransactionError(transaction, "System error: " + e.getMessage());
                transactionRepository.save(transaction);
            }
            throw new RuntimeException("Error processing payment", e);

        } finally {
            MDC.clear();
        }
    }

    @Transactional(readOnly = true)
    public PaymentResponse getTransaction(String transactionId) {
        log.debug("Retrieving transaction: {}", transactionId);
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));
        return buildPaymentResponse(transaction);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> queryTransactions(String merchantId, TransactionStatus status) {
        log.debug("Querying transactions - Merchant: {}, Status: {}", merchantId, status);

        List<Transaction> transactions;
        if (merchantId != null && status != null) {
            transactions = transactionRepository.findByMerchantIdAndStatus(merchantId, status);
        } else if (merchantId != null) {
            transactions = transactionRepository.findByMerchantIdOrderByCreatedAtDesc(merchantId);
        } else if (status != null) {
            transactions = transactionRepository.findByStatus(status);
        } else {
            transactions = transactionRepository.findAll();
        }

        return transactions.stream()
                .map(this::buildPaymentResponse)
                .toList();
    }

    private Transaction createInitialTransaction(String transactionId, PaymentRequest request) {
        return Transaction.builder()
                .transactionId(transactionId)
                .merchantId(request.getMerchantId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .cardToken(request.getCardToken())
                .cardExpiry(request.getCardExpiry())
                .operationType(request.getOperationType())
                .status(TransactionStatus.PENDING)
                .build();
    }

    private Merchant validateMerchant(String merchantId) {
        if (!merchantService.isMerchantActive(merchantId)) {
            throw new MerchantNotFoundException("Merchant not found or inactive: " + merchantId);
        }
        return merchantService.getMerchantById(merchantId);
    }

    private void validateBusinessRules(PaymentRequest request, Merchant merchant) {
        ValidationResult amountValidation = validationService.validateAmount(
                request.getAmount(), merchant);
        if (!amountValidation.isValid()) {
            throw new ValidationException(amountValidation.getReason());
        }

        ValidationResult cardValidation = validationService.validateCardToken(
                request.getCardToken());
        if (!cardValidation.isValid()) {
            throw new ValidationException(cardValidation.getReason());
        }

        ValidationResult currencyValidation = validationService.validateCurrency(
                request.getCurrency());
        if (!currencyValidation.isValid()) {
            throw new ValidationException(currencyValidation.getReason());
        }
    }

    private Transaction updateTransactionWithIssuerResponse(Transaction transaction, 
                                                           IssuerResponse issuerResponse) {
        transaction.setIssuerResponse(issuerResponse.isApproved() ? "APPROVED" : "DECLINED");
        transaction.setResponseCode(issuerResponse.getResponseCode());
        transaction.setStatus(issuerResponse.isApproved() ? 
                TransactionStatus.APPROVED : TransactionStatus.DECLINED);
        
        if (!issuerResponse.isApproved()) {
            transaction.setRejectionReason(issuerResponse.getMessage());
        }
        
        transaction.setProcessedAt(LocalDateTime.now());
        return transaction;
    }

    private Transaction handleTransactionError(Transaction transaction, String errorMessage) {
        transaction.setStatus(TransactionStatus.ERROR);
        transaction.setRejectionReason(errorMessage);
        transaction.setResponseCode("99");
        transaction.setProcessedAt(LocalDateTime.now());
        return transaction;
    }

    private PaymentResponse buildPaymentResponse(Transaction transaction) {
        String message = switch (transaction.getStatus()) {
            case APPROVED -> "Transaction approved";
            case DECLINED -> transaction.getRejectionReason() != null ? 
                    transaction.getRejectionReason() : "Transaction declined";
            case ERROR -> transaction.getRejectionReason() != null ? 
                    transaction.getRejectionReason() : "Transaction error";
            case PENDING -> "Transaction pending";
        };

        return PaymentResponse.builder()
                .transactionId(transaction.getTransactionId())
                .status(transaction.getStatus())
                .responseCode(transaction.getResponseCode())
                .message(message)
                .timestamp(transaction.getCreatedAt())
                .merchantId(transaction.getMerchantId())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .build();
    }

    private String maskCardToken(String cardToken) {
        if (cardToken == null || cardToken.length() < 4) {
            return "****";
        }
        return "**** " + cardToken.substring(cardToken.length() - 4);
    }
}
