package com.example.backend.model.email;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "email_templates")
public class EmailTemplate {
    @Id
    private String id;

    @Indexed(unique = true)
    private String name;

    private String subject;
    private String htmlContent;
    private String textContent;

    private EmailType emailType;
    private boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}