package com.example.backend.dto.response;

import lombok.Data;

@Data
public class DailyRevenue {
    private String date;
    private long revenue;
    private int transactions;
}
