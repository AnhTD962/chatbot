package com.example.backend.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentResponse {
    private String orderId;
    private String paymentUrl;
    private long amount;
    private String status;
    private LocalDateTime createdAt;
}
