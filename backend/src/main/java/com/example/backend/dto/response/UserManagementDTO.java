package com.example.backend.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserManagementDTO {
    private String id;
    private String email;
    private String fullName;
    private String role;
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private long totalMessages;
    private LocalDateTime premiumExpiresAt;
}
