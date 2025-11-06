package com.example.backend.controller;

import com.example.backend.dto.request.ChatRequest;
import com.example.backend.dto.response.ChatResponse;
import com.example.backend.service.chat.EnhancedChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class WebSocketChatController {

    private final EnhancedChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatRequest request,
                            Principal principal,
                            SimpMessageHeaderAccessor headerAccessor) {

        String username = principal != null ? principal.getName() : null;

        // Send typing indicator
        if (username != null) {
            messagingTemplate.convertAndSendToUser(
                    username,
                    "/queue/typing",
                    Map.of("typing", true)
            );
        }

        try {
            // Process chat message
            ChatResponse response = chatService.chat(request, username);

            // Send response back to user
            if (username != null) {
                messagingTemplate.convertAndSendToUser(
                        username,
                        "/queue/messages",
                        response
                );
            } else {
                // For guest users, use fingerprint
                String fingerprint = request.getGuestFingerprint();
                messagingTemplate.convertAndSend(
                        "/queue/guest/" + fingerprint,
                        response
                );
            }

            // Notify admin dashboard about new message
            messagingTemplate.convertAndSend("/topic/admin/activity",
                    Map.of(
                            "event", "message_sent",
                            "user", username != null ? username : "guest",
                            "timestamp", LocalDateTime.now(),
                            "messageLength", request.getMessage().length()
                    )
            );

        } catch (Exception e) {
            // Send error message
            ChatResponse errorResponse = new ChatResponse();
            errorResponse.setResponse("Xin lỗi, đã có lỗi xảy ra. Vui lòng thử lại.");
            errorResponse.setTimestamp(LocalDateTime.now());

            if (username != null) {
                messagingTemplate.convertAndSendToUser(
                        username,
                        "/queue/messages",
                        errorResponse
                );
            }
        } finally {
            // Stop typing indicator
            if (username != null) {
                messagingTemplate.convertAndSendToUser(
                        username,
                        "/queue/typing",
                        Map.of("typing", false)
                );
            }
        }
    }

    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload Map<String, Object> payload, Principal principal) {
        // Broadcast typing status to admins for monitoring
        if (principal != null) {
            messagingTemplate.convertAndSend("/topic/admin/typing",
                    Map.of(
                            "user", principal.getName(),
                            "typing", payload.get("typing"),
                            "timestamp", LocalDateTime.now()
                    )
            );
        }
    }

    @MessageMapping("/chat.feedback")
    public void handleFeedback(@Payload Map<String, Object> feedback, Principal principal) {
        // Save feedback and notify admins
        messagingTemplate.convertAndSend("/topic/admin/feedback",
                Map.of(
                        "user", principal != null ? principal.getName() : "guest",
                        "rating", feedback.get("rating"),
                        "comment", feedback.get("comment"),
                        "messageId", feedback.get("messageId"),
                        "timestamp", LocalDateTime.now()
                )
        );
    }
}
