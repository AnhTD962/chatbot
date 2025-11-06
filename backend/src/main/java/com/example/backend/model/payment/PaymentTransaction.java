package com.example.backend.model.payment;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "payment_transactions")
public class PaymentTransaction {
    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String orderId; // Unique order ID

    private String planType; // MONTHLY, YEARLY
    private long amount; // VND
    private String currency = "VND";

    @Indexed
    private PaymentStatus status;

    private PaymentMethod paymentMethod;

    // VNPay specific fields
    private String vnpTxnRef; // VNPay transaction reference
    private String vnpTransactionNo; // VNPay transaction number
    private String vnpBankCode; // Bank code
    private String vnpCardType; // ATM/VISA/MASTERCARD

    // Timestamps
    @Indexed
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime expiredAt;

    // Additional info
    private String description;
    private String ipAddress;
    private String userAgent;

    // Refund info
    private boolean refunded;
    private LocalDateTime refundedAt;
    private String refundReason;
}

