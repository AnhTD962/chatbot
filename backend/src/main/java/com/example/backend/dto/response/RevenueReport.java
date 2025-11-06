package com.example.backend.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RevenueReport {
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private long totalRevenue;
    private long totalTransactions;
    private long successfulTransactions;
    private long failedTransactions;

    private double successRate;
    private long averageTransactionValue;

    private int newPremiumUsers;
    private int renewedSubscriptions;
    private int cancelledSubscriptions;

    private List<DailyRevenue> dailyBreakdown;
    private List<PaymentMethodStats> paymentMethodBreakdown;
    private List<PlanTypeStats> planTypeBreakdown;
}
