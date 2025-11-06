package com.example.backend.model.email;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Document(collection = "email_logs")
public class EmailLog {
    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String recipientEmail;

    private String subject;
    private String templateName;
    private EmailType emailType;

    @Indexed
    private EmailStatus status;

    private String errorMessage;
    private int retryCount;

    @Indexed
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;

    private Map<String, Object> templateData;
}

