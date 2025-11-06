package com.example.backend.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserInfo {
    private String id;
    private String email;
    private String fullName;
    private String role;
    private boolean isPremium;
    private LocalDateTime premiumExpiresAt;
}
