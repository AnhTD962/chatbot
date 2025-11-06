package com.example.backend.repository;

import com.example.backend.model.document.ProcessedDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends MongoRepository<ProcessedDocument, String> {
    List<ProcessedDocument> findByUserId(String userId);

    List<ProcessedDocument> findByUserIdOrderByProcessedAtDesc(String userId);
}
