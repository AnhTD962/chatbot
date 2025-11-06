package com.example.backend.model.email;

public enum EmailType {
    WELCOME,                    // Đăng ký tài khoản
    PASSWORD_RESET,             // Quên mật khẩu
    PAYMENT_SUCCESS,            // Thanh toán thành công
    PAYMENT_FAILED,             // Thanh toán thất bại
    SUBSCRIPTION_EXPIRING,      // Sắp hết hạn (7 ngày trước)
    SUBSCRIPTION_EXPIRED,       // Đã hết hạn
    SUBSCRIPTION_RENEWED,       // Gia hạn thành công
    REFUND_PROCESSED,           // Hoàn tiền
    ACCOUNT_LOCKED,             // Tài khoản bị khóa
    MONTHLY_SUMMARY             // Báo cáo tháng
}
