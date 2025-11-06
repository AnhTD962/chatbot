package com.example.backend.repository;

import com.example.backend.model.chat.ChatSession;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends MongoRepository<ChatSession, String> {
    List<ChatSession> findByUserId(String userId);

    Optional<ChatSession> findByIdAndUserId(String id, String userId);
}
