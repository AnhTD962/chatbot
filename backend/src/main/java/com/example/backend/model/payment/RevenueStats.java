package com.example.backend.model.payment;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "revenue_stats")
public class RevenueStats {
    @Id
    private String id;

    @Indexed
    private LocalDateTime date; // Daily stats

    private long totalRevenue;
    private long monthlyRevenue;
    private long yearlyRevenue;

    private int newSubscriptions;
    private int renewals;
    private int cancellations;
    private int refunds;

    private long averageOrderValue;
    private double conversionRate;

    // Payment method breakdown
    private long vnpayRevenue;
    private long momoRevenue;
    private long bankTransferRevenue;
}
