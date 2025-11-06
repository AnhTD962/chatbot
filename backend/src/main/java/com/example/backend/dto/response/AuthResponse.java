package com.example.backend.dto.response;

import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private String refreshToken;
    private UserInfo user;
}
