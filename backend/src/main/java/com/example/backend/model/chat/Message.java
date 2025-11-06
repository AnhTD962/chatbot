package com.example.backend.model.chat;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Message {
    private String role; // "user" or "assistant"
    private String content;
    private LocalDateTime timestamp;
}
