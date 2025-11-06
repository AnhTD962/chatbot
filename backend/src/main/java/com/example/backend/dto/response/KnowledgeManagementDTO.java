package com.example.backend.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class KnowledgeManagementDTO {
    private String id;
    private String category;
    private String title;
    private String content;
    private List<String> tags;
    private boolean isPremium;
    private long viewCount;
    private long usageCount;
    private LocalDateTime lastUpdated;
}
