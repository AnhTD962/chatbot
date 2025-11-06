package com.example.backend.service.email;

import com.example.backend.dto.request.EmailRequest;
import com.example.backend.model.email.EmailLog;
import com.example.backend.model.email.EmailStatus;
import com.example.backend.model.email.EmailType;
import com.example.backend.model.payment.PaymentTransaction;
import com.example.backend.model.user.PasswordResetToken;
import com.example.backend.model.user.User;
import com.example.backend.repository.EmailLogRepository;
import com.example.backend.repository.PasswordResetTokenRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final EmailLogRepository emailLogRepository;
    private final PasswordResetTokenRepository tokenRepository;

    @Value("${spring.mail.from}")
    private String fromEmail;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Async
    public void sendWelcomeEmail(User user) {
        Map<String, Object> variables = Map.of(
                "userName", user.getFullName(),
                "email", user.getEmail(),
                "loginUrl", frontendUrl + "/login"
        );

        EmailRequest request = new EmailRequest();
        request.setTo(user.getEmail());
        request.setSubject("Chào mừng bạn đến với Hòa Phát Chatbot");
        request.setTemplateName("welcome");
        request.setVariables(variables);
        request.setEmailType(EmailType.WELCOME);

        sendEmail(request, user.getId());
    }

    @Async
    public void sendPasswordResetEmail(User user) {
        // Generate reset token
        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUserId(user.getId());
        resetToken.setToken(token);
        resetToken.setExpiresAt(LocalDateTime.now().plusHours(1));
        resetToken.setCreatedAt(LocalDateTime.now());
        resetToken.setUsed(false);
        tokenRepository.save(resetToken);

        String resetUrl = frontendUrl + "/reset-password?token=" + token;

        Map<String, Object> variables = Map.of(
                "userName", user.getFullName(),
                "resetUrl", resetUrl,
                "expiryTime", "1 giờ"
        );

        EmailRequest request = new EmailRequest();
        request.setTo(user.getEmail());
        request.setSubject("Đặt lại mật khẩu - Hòa Phát Chatbot");
        request.setTemplateName("password-reset");
        request.setVariables(variables);
        request.setEmailType(EmailType.PASSWORD_RESET);

        sendEmail(request, user.getId());
    }

    @Async
    public void sendPaymentSuccessEmail(User user, PaymentTransaction transaction) {
        String planName = transaction.getPlanType().equals("MONTHLY") ?
                "Premium Tháng" : "Premium Năm";

        Map<String, Object> variables = Map.of(
                "userName", user.getFullName(),
                "planName", planName,
                "amount", formatCurrency(transaction.getAmount()),
                "orderId", transaction.getOrderId(),
                "paidAt", transaction.getPaidAt().toString(),
                "expiresAt", user.getPremiumExpiresAt().toString(),
                "invoiceUrl", frontendUrl + "/invoices/" + transaction.getOrderId()
        );

        EmailRequest request = new EmailRequest();
        request.setTo(user.getEmail());
        request.setSubject("Thanh toán thành công - Hòa Phát Chatbot");
        request.setTemplateName("payment-success");
        request.setVariables(variables);
        request.setEmailType(EmailType.PAYMENT_SUCCESS);

        sendEmail(request, user.getId());
    }

    @Async
    public void sendPaymentFailedEmail(User user, PaymentTransaction transaction) {
        Map<String, Object> variables = Map.of(
                "userName", user.getFullName(),
                "orderId", transaction.getOrderId(),
                "amount", formatCurrency(transaction.getAmount()),
                "retryUrl", frontendUrl + "/premium"
        );

        EmailRequest request = new EmailRequest();
        request.setTo(user.getEmail());
        request.setSubject("Thanh toán không thành công - Hòa Phát Chatbot");
        request.setTemplateName("payment-failed");
        request.setVariables(variables);
        request.setEmailType(EmailType.PAYMENT_FAILED);

        sendEmail(request, user.getId());
    }

    @Async
    public void sendSubscriptionExpiringEmail(User user, int daysLeft) {
        Map<String, Object> variables = Map.of(
                "userName", user.getFullName(),
                "daysLeft", daysLeft,
                "expiresAt", user.getPremiumExpiresAt().toString(),
                "renewUrl", frontendUrl + "/premium"
        );

        EmailRequest request = new EmailRequest();
        request.setTo(user.getEmail());
        request.setSubject("Gói Premium sắp hết hạn - Hòa Phát Chatbot");
        request.setTemplateName("subscription-expiring");
        request.setVariables(variables);
        request.setEmailType(EmailType.SUBSCRIPTION_EXPIRING);

        sendEmail(request, user.getId());
    }

    @Async
    public void sendSubscriptionExpiredEmail(User user) {
        Map<String, Object> variables = Map.of(
                "userName", user.getFullName(),
                "renewUrl", frontendUrl + "/premium"
        );

        EmailRequest request = new EmailRequest();
        request.setTo(user.getEmail());
        request.setSubject("Gói Premium đã hết hạn - Hòa Phát Chatbot");
        request.setTemplateName("subscription-expired");
        request.setVariables(variables);
        request.setEmailType(EmailType.SUBSCRIPTION_EXPIRED);

        sendEmail(request, user.getId());
    }

    @Async
    public void sendRefundEmail(User user, PaymentTransaction transaction, String reason) {
        Map<String, Object> variables = Map.of(
                "userName", user.getFullName(),
                "orderId", transaction.getOrderId(),
                "amount", formatCurrency(transaction.getAmount()),
                "reason", reason,
                "refundedAt", transaction.getRefundedAt().toString()
        );

        EmailRequest request = new EmailRequest();
        request.setTo(user.getEmail());
        request.setSubject("Hoàn tiền thành công - Hòa Phát Chatbot");
        request.setTemplateName("refund-processed");
        request.setVariables(variables);
        request.setEmailType(EmailType.REFUND_PROCESSED);

        sendEmail(request, user.getId());
    }

    private void sendEmail(EmailRequest emailRequest, String userId) {
        EmailLog emaillog = new EmailLog();
        emaillog.setUserId(userId);
        emaillog.setRecipientEmail(emailRequest.getTo());
        emaillog.setSubject(emailRequest.getSubject());
        emaillog.setTemplateName(emailRequest.getTemplateName());
        emaillog.setEmailType(emailRequest.getEmailType());
        emaillog.setStatus(EmailStatus.PENDING);
        emaillog.setCreatedAt(LocalDateTime.now());
        emaillog.setTemplateData(emailRequest.getVariables());
        emaillog.setRetryCount(0);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(emailRequest.getTo());
            helper.setSubject(emailRequest.getSubject());

            // Process template
            Context context = new Context();
            emailRequest.getVariables().forEach(context::setVariable);

            String htmlContent = templateEngine.process(
                    emailRequest.getTemplateName(),
                    context
            );
            helper.setText(htmlContent, true);

            // Send email
            mailSender.send(message);

            emaillog.setStatus(EmailStatus.SENT);
            emaillog.setSentAt(LocalDateTime.now());

            log.info("Email sent successfully to: {}", emailRequest.getTo());

        } catch (MessagingException e) {
            emaillog.setStatus(EmailStatus.FAILED);
            emaillog.setErrorMessage(e.getMessage());
            log.error("Failed to send email to: {}", emailRequest.getTo(), e);
        } finally {
            emailLogRepository.save(emaillog);
        }
    }

    public boolean verifyResetToken(String token) {
        return tokenRepository.findByToken(token)
                .map(t -> !t.isUsed() && t.getExpiresAt().isAfter(LocalDateTime.now()))
                .orElse(false);
    }

    public void markTokenAsUsed(String token) {
        tokenRepository.findByToken(token).ifPresent(t -> {
            t.setUsed(true);
            tokenRepository.save(t);
        });
    }

    private String formatCurrency(long amount) {
        return String.format("%,d VND", amount);
    }
}
