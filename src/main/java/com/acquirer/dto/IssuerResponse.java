package com.acquirer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssuerResponse {

    private boolean approved;
    private String responseCode;
    private String message;

    public static IssuerResponse approved() {
        return IssuerResponse.builder()
                .approved(true)
                .responseCode("00")
                .message("Approved")
                .build();
    }

    public static IssuerResponse declined(String code, String message) {
        return IssuerResponse.builder()
                .approved(false)
                .responseCode(code)
                .message(message)
                .build();
    }
}
