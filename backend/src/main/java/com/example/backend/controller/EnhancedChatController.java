package com.example.backend.controller;

import com.example.backend.dto.request.ChatRequest;
import com.example.backend.dto.response.ChatResponse;
import com.example.backend.service.chat.EnhancedChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class EnhancedChatController {

    private final EnhancedChatService chatService;

    // Guest endpoint - no authentication required
    @PostMapping("/guest")
    public ResponseEntity<ChatResponse> guestChat(@RequestBody ChatRequest request) {
        ChatResponse response = chatService.chat(request, null);
        return ResponseEntity.ok(response);
    }

    // Authenticated endpoint
    @PostMapping
    public ResponseEntity<ChatResponse> authenticatedChat(
            @RequestBody ChatRequest request,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        ChatResponse response = chatService.chat(request, userEmail);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Chatbot service is running");
    }

}

