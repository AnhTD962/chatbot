package com.example.backend.model.analytics;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "user_feedback")
public class UserFeedback {
    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String messageId;

    private int rating; // 1-5 stars
    private String comment;
    private String category; // "helpful", "unhelpful", "bug", "suggestion"

    @Indexed
    private LocalDateTime createdAt;
}
