package com.example.backend.model.analytics;

import lombok.Data;

@Data
public class HourlyStats {
    private int hour;
    private long messageCount;
    private long activeUsers;
}
