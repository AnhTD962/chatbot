package com.example.backend.dto.request;

import lombok.Data;

@Data
public class ChatRequest {
    private String sessionId;
    private String message;
    private String userId;
    private String guestFingerprint; // For guest users
    private boolean requestDetailedInfo; // Premium feature flag
}
