package com.example.backend.controller;

import com.example.backend.dto.request.PaymentRequest;
import com.example.backend.dto.response.PaymentResponse;
import com.example.backend.model.payment.PaymentStatus;
import com.example.backend.model.payment.PaymentTransaction;
import com.example.backend.model.user.User;
import com.example.backend.model.user.UserRole;
import com.example.backend.service.payment.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final VNPayService vnPayService;

    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaymentResponse> createPayment(
            @RequestBody PaymentRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        User user = (User) authentication.getPrincipal();
        request.setUserId(user.getId());

        String ipAddress = getClientIp(httpRequest);
        PaymentResponse response = vnPayService.createPayment(request, ipAddress);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/callback")
    public ResponseEntity<Map<String, Object>> paymentCallback(
            @RequestParam Map<String, String> params) {

        try {
            PaymentTransaction transaction = vnPayService.handleCallback(params);

            return ResponseEntity.ok(Map.of(
                    "success", transaction.getStatus() == PaymentStatus.COMPLETED,
                    "orderId", transaction.getOrderId(),
                    "status", transaction.getStatus().name(),
                    "amount", transaction.getAmount(),
                    "message", transaction.getStatus() == PaymentStatus.COMPLETED
                            ? "Thanh toán thành công! Tài khoản Premium đã được kích hoạt."
                            : "Thanh toán thất bại. Vui lòng thử lại."
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/transaction/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getTransaction(
            @PathVariable String orderId,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();
        PaymentTransaction transaction = vnPayService.getTransactionByOrderId(orderId);

        // Nếu không tìm thấy giao dịch
        if (transaction == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "Không tìm thấy giao dịch với mã đơn hàng: " + orderId
            ));
        }

        // Kiểm tra quyền truy cập
        boolean isOwner = transaction.getUserId().equals(user.getId());
        boolean isAdmin = user.getRole().equals(UserRole.ADMIN);

        if (!isOwner && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", "Bạn không có quyền xem giao dịch này"
            ));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", transaction
        ));
    }

    @GetMapping("/my-transactions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PaymentTransaction>> getMyTransactions(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<PaymentTransaction> transactions = vnPayService.getUserTransactions(user.getId());
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/plans")
    public ResponseEntity<List<Map<String, Object>>> getPlans() {
        return ResponseEntity.ok(List.of(
                Map.of(
                        "type", "MONTHLY",
                        "name", "Premium Tháng",
                        "price", 50000L,
                        "duration", 30,
                        "savings", 0,
                        "features", List.of(
                                "Không giới hạn câu hỏi",
                                "Thông tin tài chính chi tiết",
                                "Phân tích chuyên sâu",
                                "Báo cáo & tài liệu nội bộ"
                        )
                ),
                Map.of(
                        "type", "YEARLY",
                        "name", "Premium Năm",
                        "price", 500000L,
                        "originalPrice", 600000L,
                        "duration", 365,
                        "savings", 17,
                        "popular", true,
                        "features", List.of(
                                "Tất cả tính năng Premium Tháng",
                                "Tiết kiệm 100.000đ (17%)",
                                "Hỗ trợ ưu tiên",
                                "Cập nhật sớm tính năng mới"
                        )
                )
        ));
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
