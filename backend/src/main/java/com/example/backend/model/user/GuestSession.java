package com.example.backend.model.user;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "guest_sessions")
public class GuestSession {
    @Id
    private String id;

    @Indexed
    private String ipAddress;

    @Indexed
    private String fingerprint; // Browser fingerprint

    private int questionCount;
    private LocalDateTime createdAt;
    private LocalDateTime lastUsed;

    private LocalDateTime ExpiresAt;
}
