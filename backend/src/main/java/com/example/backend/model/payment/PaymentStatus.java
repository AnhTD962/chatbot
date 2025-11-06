package com.example.backend.model.payment;

public enum PaymentStatus {
    PENDING,      // Chờ thanh toán
    PROCESSING,   // Đang xử lý
    COMPLETED,    // Thành công
    FAILED,       // Thất bại
    CANCELLED,    // Đã hủy
    REFUNDED      // Đã hoàn tiền
}
