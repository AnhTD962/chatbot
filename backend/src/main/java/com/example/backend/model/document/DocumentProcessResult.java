package com.example.backend.model.document;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DocumentProcessResult {
    private String documentId;
    private String fileName;
    private int pageCount;
    private int wordCount;
    private String extractedText;
    private String summary;
    private List<String> keywords;
    private Map<String, List<String>> entities;
    private List<String> tables;
}
