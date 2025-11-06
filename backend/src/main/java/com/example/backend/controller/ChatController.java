//package com.example.backend.controller;
//
//import com.example.backend.dto.request.ChatRequest;
//import com.example.backend.dto.response.ChatResponse;
//import com.example.backend.model.chat.ChatSession;
//import com.example.backend.service.chat.ChatService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/chat")
//@RequiredArgsConstructor
//public class ChatController {
//
//    private final ChatService chatService;
//
//    @PostMapping
//    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
//        ChatResponse response = chatService.chat(request);
//        return ResponseEntity.ok(response);
//    }
//
//    @GetMapping("/sessions/{userId}")
//    public ResponseEntity<List<ChatSession>> getUserSessions(@PathVariable String userId) {
//        List<ChatSession> sessions = chatService.getUserSessions(userId);
//        return ResponseEntity.ok(sessions);
//    }
//
//    @GetMapping("/health")
//    public ResponseEntity<String> health() {
//        return ResponseEntity.ok("Chatbot service is running");
//    }
//}
