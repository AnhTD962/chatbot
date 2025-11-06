package com.example.backend.repository;

import com.example.backend.model.payment.PaymentStatus;
import com.example.backend.model.payment.PaymentTransaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends MongoRepository<PaymentTransaction, String> {
    Optional<PaymentTransaction> findByOrderId(String orderId);
    List<PaymentTransaction> findByUserId(String userId);
    List<PaymentTransaction> findByStatus(PaymentStatus status);
    List<PaymentTransaction> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    long countByStatusAndCreatedAtBetween(PaymentStatus status, LocalDateTime start, LocalDateTime end);
}
