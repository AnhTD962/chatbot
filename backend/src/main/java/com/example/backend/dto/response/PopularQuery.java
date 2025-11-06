package com.example.backend.dto.response;

import lombok.Data;

@Data
public class PopularQuery {
    private String query;
    private long count;
    private double avgSatisfaction;
}
