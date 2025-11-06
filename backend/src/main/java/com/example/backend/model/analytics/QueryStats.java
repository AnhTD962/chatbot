package com.example.backend.model.analytics;

import lombok.Data;

@Data
public class QueryStats {
    private String query;
    private long count;
    private double avgResponseTime;
}
