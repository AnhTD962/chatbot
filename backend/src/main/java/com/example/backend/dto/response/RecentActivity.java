package com.example.backend.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RecentActivity {
    private String type; // "new_user", "upgrade", "message", "feedback"
    private String description;
    private LocalDateTime timestamp;
    private String userId;
}
