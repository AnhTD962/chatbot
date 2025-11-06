package com.example.backend.model.analytics;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "chat_analytics")
public class ChatAnalytics {
    @Id
    private String id;

    @Indexed
    private LocalDateTime date;

    private long totalMessages;
    private long totalSessions;
    private long totalUsers;
    private long guestUsers;
    private long freeUsers;
    private long premiumUsers;

    private long averageMessagesPerSession;
    private double averageResponseTime; // milliseconds

    private long successfulResponses;
    private long failedResponses;

    // Popular queries
    private List<QueryStats> topQueries;

    // Peak hours
    private List<HourlyStats> hourlyBreakdown;
}
