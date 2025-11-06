package com.example.backend.model.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Document(collection = "processed_documents")
public class ProcessedDocument {
    @Id
    private String id;

    @Indexed
    private String userId;

    private String fileName;
    private long fileSize;

    @Indexed
    private String extractedText;

    private int pageCount;
    private int wordCount;

    private List<String> keywords;
    private Map<String, List<String>> entities; // companies, people, locations
    private List<Map<String, Object>> numbers;  // revenue, percentages
    private List<String> tables;
    private List<String> dates;

    @Indexed
    private LocalDateTime processedAt;
}
