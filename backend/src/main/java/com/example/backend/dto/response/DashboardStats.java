package com.example.backend.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DashboardStats {
    private long totalUsers;
    private long activeUsers;
    private long totalMessages;
    private long todayMessages;

    private long guestCount;
    private long freeCount;
    private long premiumCount;

    private double conversionRate; // Guest -> Free
    private double upgradRate; // Free -> Premium

    private long totalRevenue;
    private long monthlyRevenue;

    private List<RecentActivity> recentActivities;
    private List<PopularQuery> popularQueries;
    private List<UserGrowth> userGrowthData;
}

