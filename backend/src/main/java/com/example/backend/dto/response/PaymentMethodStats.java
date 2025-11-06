package com.example.backend.dto.response;

import lombok.Data;

@Data
public class PaymentMethodStats {
    private String method;
    private long revenue;
    private int count;
    private double percentage;
}
