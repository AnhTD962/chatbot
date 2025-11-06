package com.example.backend.repository;

import com.example.backend.model.payment.RevenueStats;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RevenueStatsRepository extends MongoRepository<RevenueStats, String> {
    Optional<RevenueStats> findByDate(LocalDateTime date);

    List<RevenueStats> findByDateBetween(LocalDateTime start, LocalDateTime end);
}
