package com.example.backend.service.payment;

import com.example.backend.dto.request.PaymentRequest;
import com.example.backend.dto.response.*;
import com.example.backend.model.payment.PaymentMethod;
import com.example.backend.model.payment.PaymentStatus;
import com.example.backend.model.payment.PaymentTransaction;
import com.example.backend.model.payment.RevenueStats;
import com.example.backend.model.user.User;
import com.example.backend.model.user.UserRole;
import com.example.backend.repository.PaymentTransactionRepository;
import com.example.backend.repository.RevenueStatsRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VNPayService {

    @Value("${vnpay.url}")
    private String vnpUrl;

    @Value("${vnpay.tmn-code}")
    private String vnpTmnCode;

    @Value("${vnpay.hash-secret}")
    private String vnpHashSecret;

    @Value("${vnpay.return-url}")
    private String vnpReturnUrl;

    private final PaymentTransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final RevenueStatsRepository revenueStatsRepository;

    // Plan pricing
    private static final Map<String, Long> PLAN_PRICES = Map.of(
            "MONTHLY", 50000L,  // 50K VND
            "YEARLY", 500000L   // 500K VND (tiết kiệm ~17%)
    );

    private static final Map<String, Integer> PLAN_DURATION = Map.of(
            "MONTHLY", 30,
            "YEARLY", 365
    );

    public PaymentResponse createPayment(PaymentRequest request, String ipAddress) {
        // Validate plan
        Long amount = PLAN_PRICES.get(request.getPlanType());
        if (amount == null) {
            throw new RuntimeException("Invalid plan type");
        }

        // Create transaction record
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setUserId(request.getUserId());
        transaction.setOrderId(generateOrderId());
        transaction.setPlanType(request.getPlanType());
        transaction.setAmount(amount);
        transaction.setStatus(PaymentStatus.PENDING);
        transaction.setPaymentMethod(PaymentMethod.VNPAY);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setIpAddress(ipAddress);
        transaction.setDescription("Nâng cấp Premium " + request.getPlanType());

        transactionRepository.save(transaction);

        // Generate VNPay payment URL
        String paymentUrl = generateVNPayUrl(transaction, ipAddress);

        PaymentResponse response = new PaymentResponse();
        response.setOrderId(transaction.getOrderId());
        response.setPaymentUrl(paymentUrl);
        response.setAmount(amount);
        response.setStatus("PENDING");
        response.setCreatedAt(transaction.getCreatedAt());

        return response;
    }

    private String generateVNPayUrl(PaymentTransaction transaction, String ipAddress) {
        Map<String, String> vnpParams = new TreeMap<>();

        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", vnpTmnCode);
        vnpParams.put("vnp_Amount", String.valueOf(transaction.getAmount() * 100)); // VNPay requires amount * 100
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", transaction.getOrderId());
        vnpParams.put("vnp_OrderInfo", transaction.getDescription());
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnpReturnUrl);
        vnpParams.put("vnp_IpAddr", ipAddress);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnpCreateDate = formatter.format(new Date());
        vnpParams.put("vnp_CreateDate", vnpCreateDate);

        // Expire after 15 minutes
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 15);
        String vnpExpireDate = formatter.format(calendar.getTime());
        vnpParams.put("vnp_ExpireDate", vnpExpireDate);

        // Build query string
        String queryString = vnpParams.entrySet().stream()
                .map(entry -> {
                    try {
                        return URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.toString()) +
                                "=" +
                                URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString());
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.joining("&"));

        // Generate secure hash
        String signData = vnpParams.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));

        String vnpSecureHash = hmacSHA512(vnpHashSecret, signData);
        queryString += "&vnp_SecureHash=" + vnpSecureHash;

        return vnpUrl + "?" + queryString;
    }

    public PaymentTransaction handleCallback(Map<String, String> params) {
        // Validate secure hash
        String vnpSecureHash = params.get("vnp_SecureHash");
        params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");

        String signData = params.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));

        String calculatedHash = hmacSHA512(vnpHashSecret, signData);

        if (!calculatedHash.equals(vnpSecureHash)) {
            log.error("Invalid VNPay signature");
            throw new RuntimeException("Invalid payment signature");
        }

        // Get transaction
        String orderId = params.get("vnp_TxnRef");
        PaymentTransaction transaction = transactionRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        // Check response code
        String responseCode = params.get("vnp_ResponseCode");

        if ("00".equals(responseCode)) {
            // Payment successful
            transaction.setStatus(PaymentStatus.COMPLETED);
            transaction.setPaidAt(LocalDateTime.now());
            transaction.setVnpTransactionNo(params.get("vnp_TransactionNo"));
            transaction.setVnpBankCode(params.get("vnp_BankCode"));
            transaction.setVnpCardType(params.get("vnp_CardType"));

            // Upgrade user to premium
            upgradeUserToPremium(transaction);

            // Update revenue stats
            updateRevenueStats(transaction);

            log.info("Payment successful for order: {}", orderId);
        } else {
            // Payment failed
            transaction.setStatus(PaymentStatus.FAILED);
            log.warn("Payment failed for order: {} with code: {}", orderId, responseCode);
        }

        transactionRepository.save(transaction);
        return transaction;
    }

    private void upgradeUserToPremium(PaymentTransaction transaction) {
        User user = userRepository.findById(transaction.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setRole(UserRole.PREMIUM);
        user.setSubscriptionPlan(transaction.getPlanType());

        int durationDays = PLAN_DURATION.get(transaction.getPlanType());
        user.setPremiumExpiresAt(LocalDateTime.now().plusDays(durationDays));

        userRepository.save(user);
        log.info("User {} upgraded to Premium {}", user.getEmail(), transaction.getPlanType());
    }

    private void updateRevenueStats(PaymentTransaction transaction) {
        LocalDateTime today = LocalDateTime.now().toLocalDate().atStartOfDay();

        RevenueStats stats = revenueStatsRepository.findByDate(today)
                .orElseGet(() -> {
                    RevenueStats newStats = new RevenueStats();
                    newStats.setDate(today);
                    newStats.setTotalRevenue(0L);
                    newStats.setNewSubscriptions(0);
                    return newStats;
                });

        stats.setTotalRevenue(stats.getTotalRevenue() + transaction.getAmount());
        stats.setNewSubscriptions(stats.getNewSubscriptions() + 1);

        if (transaction.getPlanType().equals("MONTHLY")) {
            stats.setMonthlyRevenue(stats.getMonthlyRevenue() + transaction.getAmount());
        } else {
            stats.setYearlyRevenue(stats.getYearlyRevenue() + transaction.getAmount());
        }

        stats.setVnpayRevenue(stats.getVnpayRevenue() + transaction.getAmount());

        revenueStatsRepository.save(stats);
    }

    public List<PaymentTransaction> getUserTransactions(String userId) {
        return transactionRepository.findByUserId(userId);
    }

    public PaymentTransaction getTransactionByOrderId(String orderId) {
        return transactionRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
    }

    public RevenueReport getRevenueReport(LocalDateTime startDate, LocalDateTime endDate) {
        List<PaymentTransaction> transactions = transactionRepository
                .findByCreatedAtBetween(startDate, endDate);

        RevenueReport report = new RevenueReport();
        report.setStartDate(startDate);
        report.setEndDate(endDate);

        // Calculate totals
        long totalRevenue = transactions.stream()
                .filter(t -> t.getStatus() == PaymentStatus.COMPLETED)
                .mapToLong(PaymentTransaction::getAmount)
                .sum();

        report.setTotalRevenue(totalRevenue);
        report.setTotalTransactions((long) transactions.size());

        long successfulCount = transactions.stream()
                .filter(t -> t.getStatus() == PaymentStatus.COMPLETED)
                .count();
        report.setSuccessfulTransactions(successfulCount);

        long failedCount = transactions.stream()
                .filter(t -> t.getStatus() == PaymentStatus.FAILED)
                .count();
        report.setFailedTransactions(failedCount);

        report.setSuccessRate(
                transactions.isEmpty() ? 0 : (double) successfulCount / transactions.size() * 100
        );

        report.setAverageTransactionValue(
                successfulCount == 0 ? 0 : totalRevenue / successfulCount
        );

        // Daily breakdown
        Map<String, List<PaymentTransaction>> dailyMap = transactions.stream()
                .filter(t -> t.getStatus() == PaymentStatus.COMPLETED)
                .collect(Collectors.groupingBy(
                        t -> t.getCreatedAt().toLocalDate().toString()
                ));

        List<DailyRevenue> dailyBreakdown = dailyMap.entrySet().stream()
                .map(entry -> {
                    DailyRevenue dr = new DailyRevenue();
                    dr.setDate(entry.getKey());
                    dr.setRevenue(entry.getValue().stream()
                            .mapToLong(PaymentTransaction::getAmount)
                            .sum());
                    dr.setTransactions(entry.getValue().size());
                    return dr;
                })
                .sorted(Comparator.comparing(DailyRevenue::getDate))
                .collect(Collectors.toList());
        report.setDailyBreakdown(dailyBreakdown);

        // Payment method breakdown
        Map<PaymentMethod, List<PaymentTransaction>> methodMap = transactions.stream()
                .filter(t -> t.getStatus() == PaymentStatus.COMPLETED)
                .collect(Collectors.groupingBy(PaymentTransaction::getPaymentMethod));

        List<PaymentMethodStats> methodStats = methodMap.entrySet().stream()
                .map(entry -> {
                    PaymentMethodStats pms = new PaymentMethodStats();
                    pms.setMethod(entry.getKey().name());
                    pms.setRevenue(entry.getValue().stream()
                            .mapToLong(PaymentTransaction::getAmount)
                            .sum());
                    pms.setCount(entry.getValue().size());
                    pms.setPercentage((double) entry.getValue().size() / successfulCount * 100);
                    return pms;
                })
                .collect(Collectors.toList());
        report.setPaymentMethodBreakdown(methodStats);

        // Plan type breakdown
        Map<String, List<PaymentTransaction>> planMap = transactions.stream()
                .filter(t -> t.getStatus() == PaymentStatus.COMPLETED)
                .collect(Collectors.groupingBy(PaymentTransaction::getPlanType));

        List<PlanTypeStats> planStats = planMap.entrySet().stream()
                .map(entry -> {
                    PlanTypeStats pts = new PlanTypeStats();
                    pts.setPlanType(entry.getKey());
                    pts.setRevenue(entry.getValue().stream()
                            .mapToLong(PaymentTransaction::getAmount)
                            .sum());
                    pts.setCount(entry.getValue().size());
                    pts.setPercentage((double) entry.getValue().size() / successfulCount * 100);
                    return pts;
                })
                .collect(Collectors.toList());
        report.setPlanTypeBreakdown(planStats);

        return report;
    }

    private String generateOrderId() {
        return "HPG" + System.currentTimeMillis();
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : result) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generating HMAC", e);
        }
    }
}
