package com.example.backend.dto.response;

import lombok.Data;

@Data
public class PlanTypeStats {
    private String planType;
    private long revenue;
    private int count;
    private double percentage;
}
