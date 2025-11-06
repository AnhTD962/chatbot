package com.example.backend.controller;

import com.example.backend.dto.request.EmailRequest;
import com.example.backend.model.email.EmailLog;
import com.example.backend.model.email.EmailStatus;
import com.example.backend.model.email.EmailType;
import com.example.backend.repository.EmailLogRepository;
import com.example.backend.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/emails")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminEmailController {

    private final EmailLogRepository emailLogRepository;
    private final EmailService emailService;

    @GetMapping("/logs")
    public ResponseEntity<List<EmailLog>> getEmailLogs(
            @RequestParam(required = false) EmailStatus status,
            @RequestParam(required = false) EmailType type) {

        List<EmailLog> logs;

        if (status != null) {
            logs = emailLogRepository.findByStatus(status);
        } else if (type != null) {
            logs = emailLogRepository.findByEmailType(type);
        } else {
            logs = emailLogRepository.findAll();
        }

        return ResponseEntity.ok(logs);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getEmailStats() {
        LocalDateTime today = LocalDateTime.now().toLocalDate().atStartOfDay();

        long sentToday = emailLogRepository.countByStatusAndCreatedAtAfter(
                EmailStatus.SENT, today
        );

        long failedToday = emailLogRepository.countByStatusAndCreatedAtAfter(
                EmailStatus.FAILED, today
        );

        return ResponseEntity.ok(Map.of(
                "sentToday", sentToday,
                "failedToday", failedToday,
                "successRate", sentToday + failedToday == 0 ? 0 :
                        (double) sentToday / (sentToday + failedToday) * 100
        ));
    }

    @PostMapping("/test")
    public ResponseEntity<Map<String, String>> sendTestEmail(
            @RequestBody Map<String, String> request) {

        String to = request.get("to");
        String templateName = request.get("template");

        // Send test email
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setTo(to);
        emailRequest.setSubject("Test Email - Hòa Phát Chatbot");
        emailRequest.setTemplateName(templateName);
        emailRequest.setVariables(Map.of(
                "userName", "Admin Test",
                "email", to
        ));

        return ResponseEntity.ok(Map.of(
                "message", "Test email sent to " + to
        ));
    }
}
