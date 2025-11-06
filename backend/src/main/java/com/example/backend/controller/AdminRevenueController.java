package com.example.backend.controller;

import com.example.backend.dto.response.RevenueReport;
import com.example.backend.model.payment.PaymentStatus;
import com.example.backend.model.payment.PaymentTransaction;
import com.example.backend.model.payment.RevenueStats;
import com.example.backend.repository.PaymentTransactionRepository;
import com.example.backend.repository.RevenueStatsRepository;
import com.example.backend.service.payment.VNPayService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/revenue")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminRevenueController {

    private final VNPayService vnPayService;
    private final PaymentTransactionRepository transactionRepository;
    private final RevenueStatsRepository revenueStatsRepository;

    @GetMapping("/report")
    public ResponseEntity<RevenueReport> getRevenueReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        RevenueReport report = vnPayService.getRevenueReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getRevenueDashboard() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
        LocalDateTime startOfYear = now.withDayOfYear(1).toLocalDate().atStartOfDay();
        LocalDateTime yesterday = now.minusDays(1).toLocalDate().atStartOfDay();
        LocalDateTime lastMonth = now.minusMonths(1).withDayOfMonth(1).toLocalDate().atStartOfDay();
        LocalDateTime endOfLastMonth = now.withDayOfMonth(1).minusDays(1).toLocalDate().atStartOfDay();

        // Today's revenue
        long todayRevenue = transactionRepository
                .findByCreatedAtBetween(now.toLocalDate().atStartOfDay(), now)
                .stream()
                .filter(t -> t.getStatus() == PaymentStatus.COMPLETED)
                .mapToLong(PaymentTransaction::getAmount)
                .sum();

        // Yesterday's revenue
        long yesterdayRevenue = transactionRepository
                .findByCreatedAtBetween(yesterday, now.toLocalDate().atStartOfDay())
                .stream()
                .filter(t -> t.getStatus() == PaymentStatus.COMPLETED)
                .mapToLong(PaymentTransaction::getAmount)
                .sum();

        // This month's revenue
        long monthRevenue = transactionRepository
                .findByCreatedAtBetween(startOfMonth, now)
                .stream()
                .filter(t -> t.getStatus() == PaymentStatus.COMPLETED)
                .mapToLong(PaymentTransaction::getAmount)
                .sum();

        // Last month's revenue
        long lastMonthRevenue = transactionRepository
                .findByCreatedAtBetween(lastMonth, endOfLastMonth)
                .stream()
                .filter(t -> t.getStatus() == PaymentStatus.COMPLETED)
                .mapToLong(PaymentTransaction::getAmount)
                .sum();

        // This year's revenue
        long yearRevenue = transactionRepository
                .findByCreatedAtBetween(startOfYear, now)
                .stream()
                .filter(t -> t.getStatus() == PaymentStatus.COMPLETED)
                .mapToLong(PaymentTransaction::getAmount)
                .sum();

        // Calculate growth rates
        double dailyGrowth = yesterdayRevenue == 0 ? 0 :
                ((double) (todayRevenue - yesterdayRevenue) / yesterdayRevenue * 100);

        double monthlyGrowth = lastMonthRevenue == 0 ? 0 :
                ((double) (monthRevenue - lastMonthRevenue) / lastMonthRevenue * 100);

        // Transaction stats
        long totalTransactions = transactionRepository.count();
        long successfulTransactions = transactionRepository.countByStatusAndCreatedAtBetween(
                PaymentStatus.COMPLETED, startOfMonth, now
        );
        long failedTransactions = transactionRepository.countByStatusAndCreatedAtBetween(
                PaymentStatus.FAILED, startOfMonth, now
        );

        return ResponseEntity.ok(Map.ofEntries(
                Map.entry("todayRevenue", todayRevenue),
                Map.entry("yesterdayRevenue", yesterdayRevenue),
                Map.entry("monthRevenue", monthRevenue),
                Map.entry("lastMonthRevenue", lastMonthRevenue),
                Map.entry("yearRevenue", yearRevenue),
                Map.entry("dailyGrowth", dailyGrowth),
                Map.entry("monthlyGrowth", monthlyGrowth),
                Map.entry("totalTransactions", totalTransactions),
                Map.entry("successfulTransactions", successfulTransactions),
                Map.entry("failedTransactions", failedTransactions),
                Map.entry("successRate", totalTransactions == 0 ? 0 :
                        (double) successfulTransactions / totalTransactions * 100)
        ));
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<PaymentTransaction>> getAllTransactions(
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        List<PaymentTransaction> transactions;

        if (startDate != null && endDate != null) {
            transactions = transactionRepository.findByCreatedAtBetween(startDate, endDate);
        } else if (status != null) {
            transactions = transactionRepository.findByStatus(status);
        } else {
            transactions = transactionRepository.findAll();
        }

        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/stats/daily")
    public ResponseEntity<List<RevenueStats>> getDailyStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime endDate) {

        List<RevenueStats> stats = revenueStatsRepository.findByDateBetween(startDate, endDate);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        // TODO: Generate Excel/CSV export
        RevenueReport report = vnPayService.getRevenueReport(startDate, endDate);

        // For now, return JSON
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=revenue-report.json")
                .body(report.toString().getBytes());
    }

    @PostMapping("/refund/{orderId}")
    public ResponseEntity<Map<String, Object>> refundTransaction(
            @PathVariable String orderId,
            @RequestParam String reason) {

        PaymentTransaction transaction = vnPayService.getTransactionByOrderId(orderId);

        if (transaction.getStatus() != PaymentStatus.COMPLETED) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Chỉ có thể hoàn tiền cho giao dịch thành công"
            ));
        }

        // Update transaction
        transaction.setStatus(PaymentStatus.REFUNDED);
        transaction.setRefunded(true);
        transaction.setRefundedAt(LocalDateTime.now());
        transaction.setRefundReason(reason);
        transactionRepository.save(transaction);

        // Downgrade user (optional - you might want to keep until expiry)
        // ... implementation

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Hoàn tiền thành công"
        ));
    }
}
