package com.example.backend.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    // Track active users
    private final Map<String, String> activeSessions = new ConcurrentHashMap<>();

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String username = headerAccessor.getUser() != null ?
                headerAccessor.getUser().getName() : "anonymous";

        activeSessions.put(sessionId, username);
        log.info("New WebSocket connection: {} (session: {})", username, sessionId);

        // Notify admins about new connection
        messagingTemplate.convertAndSend("/topic/admin/connections",
                Map.of(
                        "event", "user_connected",
                        "username", username,
                        "sessionId", sessionId,
                        "activeUsers", activeSessions.size()
                )
        );
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String username = activeSessions.remove(sessionId);

        if (username != null) {
            log.info("User disconnected: {} (session: {})", username, sessionId);

            // Notify admins about disconnection
            messagingTemplate.convertAndSend("/topic/admin/connections",
                    Map.of(
                            "event", "user_disconnected",
                            "username", username,
                            "sessionId", sessionId,
                            "activeUsers", activeSessions.size()
                    )
            );
        }
    }

    public int getActiveUserCount() {
        return activeSessions.size();
    }

    public Map<String, String> getActiveSessions() {
        return new ConcurrentHashMap<>(activeSessions);
    }
}
