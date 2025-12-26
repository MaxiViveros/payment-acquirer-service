package com.acquirer.service;

import com.acquirer.dto.IssuerResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;

@Service
@Slf4j
public class IssuerService {

    private final Random random = new Random();

    @Value("${acquirer.rules.issuer.approval-rate:0.7}")
    private double approvalRate;

    /**
     * Process authorization request with the issuer (mocked)
     * 
     * @param cardToken Card token/hash
     * @param amount Transaction amount
     * @param currency Currency code
     * @return IssuerResponse with approval/decline decision
     */
    public IssuerResponse authorizeTransaction(String cardToken, BigDecimal amount, String currency) {
        log.debug("Calling issuer for authorization - Card: {}, Amount: {} {}", 
                  maskCardToken(cardToken), amount, currency);

        try {
            Thread.sleep(100 + random.nextInt(200)); // 100-300ms delay (simulated)
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Issuer call interrupted", e);
        }

        // Random approval/decline based on configured rate
        boolean approved = random.nextDouble() < approvalRate;

        IssuerResponse response;
        if (approved) {
            response = IssuerResponse.approved();
            log.info("Issuer APPROVED transaction - Card: {}", maskCardToken(cardToken));
        } else {
            String declineCode = getRandomDeclineCode();
            String declineMessage = getDeclineMessage(declineCode);
            response = IssuerResponse.declined(declineCode, declineMessage);
            log.info("Issuer DECLINED transaction - Card: {}, Code: {}, Reason: {}", 
                     maskCardToken(cardToken), declineCode, declineMessage);
        }

        return response;
    }

    private String getRandomDeclineCode() {
        String[] declineCodes = {"05", "51", "54", "61", "65"};
        return declineCodes[random.nextInt(declineCodes.length)];
    }

    private String getDeclineMessage(String code) {
        return switch (code) {
            case "05" -> "Do not honor";
            case "51" -> "Insufficient funds";
            case "54" -> "Expired card";
            case "61" -> "Exceeds withdrawal limit";
            case "65" -> "Activity limit exceeded";
            default -> "Transaction declined";
        };
    }
    
    private String maskCardToken(String cardToken) {
        if (cardToken == null || cardToken.length() < 4) {
            return "****";
        }
        return "**** " + cardToken.substring(cardToken.length() - 4);
    }
}
