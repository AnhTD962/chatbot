package com.example.backend.model.user;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "password_reset_tokens")
public class PasswordResetToken {
    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String token;

    @Indexed
    private LocalDateTime expiresAt;

    private boolean used;
    private LocalDateTime createdAt;
}
