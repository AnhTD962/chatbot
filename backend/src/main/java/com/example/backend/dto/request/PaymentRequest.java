package com.example.backend.dto.request;

import lombok.Data;

@Data
public class PaymentRequest {
    private String userId;
    private String planType; // MONTHLY, YEARLY
    private String returnUrl;
    private String cancelUrl;
}
