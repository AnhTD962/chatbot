package com.example.backend.model.knowledge;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "knowledge_updates")
public class KnowledgeUpdate {
    @Id
    private String id;

    @Indexed
    private String knowledgeId;

    private String action; // "CREATE", "UPDATE", "DELETE"
    private String adminId;
    private String adminName;

    private String oldContent;
    private String newContent;

    @Indexed
    private LocalDateTime timestamp;
}
