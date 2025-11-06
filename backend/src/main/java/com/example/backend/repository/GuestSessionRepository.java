package com.example.backend.repository;

import com.example.backend.model.user.GuestSession;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GuestSessionRepository extends MongoRepository<GuestSession, String> {
    Optional<GuestSession> findByFingerprint(String fingerprint);

    Optional<GuestSession> findByIpAddress(String ipAddress);
}
