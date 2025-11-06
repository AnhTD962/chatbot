package com.example.backend.model.chat;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "chat_sessions")
public class ChatSession {
    @Id
    private String id;
    private String userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Message> messages;
}
