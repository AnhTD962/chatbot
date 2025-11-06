package com.example.backend.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ChatResponse {
    private String sessionId;
    private String response;
    private LocalDateTime timestamp;
    private List<String> sources;

    // New fields for guest/premium features
    private Integer remainingQuestions; // For guest users
    private boolean isGuest;
    private boolean limitReached;
    private boolean premiumRequired;
}
