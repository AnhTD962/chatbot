package com.example.backend.model.knowledge;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "hoaphat_knowledge")
public class HoaphatKnowledge {
    @Id
    private String id;
    private String category; // "company_info", "products", "financial", "news"
    private String title;
    private String content;
    private List<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
