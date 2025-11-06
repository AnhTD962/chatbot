package com.example.backend.repository;

import com.example.backend.model.email.EmailLog;
import com.example.backend.model.email.EmailStatus;
import com.example.backend.model.email.EmailType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EmailLogRepository extends MongoRepository<EmailLog, String> {
    List<EmailLog> findByUserId(String userId);
    List<EmailLog> findByStatus(EmailStatus status);
    List<EmailLog> findByEmailType(EmailType emailType);
    List<EmailLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    long countByStatusAndCreatedAtAfter(EmailStatus status, LocalDateTime date);
}
