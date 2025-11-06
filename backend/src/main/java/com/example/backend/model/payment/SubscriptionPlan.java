package com.example.backend.model.payment;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "subscription_plans")
public class SubscriptionPlan {
    @Id
    private String id;

    private String name; // "Premium Monthly", "Premium Yearly"
    private String planType; // MONTHLY, YEARLY
    private long price; // VND
    private int durationDays;

    private String description;
    private List<String> features;

    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
