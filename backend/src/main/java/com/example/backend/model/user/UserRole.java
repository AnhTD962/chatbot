package com.example.backend.model.user;

public enum UserRole {
    FREE,      // Giới hạn 5 câu/session
    PREMIUM,   // Không giới hạn + thông tin chi tiết
    ADMIN      // Full access
}
