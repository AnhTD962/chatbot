package com.example.backend.model.analytics;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "system_stats")
public class SystemStats {
    @Id
    private String id;

    @Indexed
    private LocalDateTime timestamp;

    private int activeConnections;
    private long totalApiCalls;
    private long successfulCalls;
    private long failedCalls;

    private double avgResponseTime;
    private double cpuUsage;
    private long memoryUsage;

    private long databaseSize;
    private long totalKnowledgeEntries;
}

