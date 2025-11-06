package com.example.backend.scheduler;

import com.example.backend.model.user.User;
import com.example.backend.model.user.UserRole;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionScheduler {

    private final UserRepository userRepository;
    private final EmailService emailService;

    /**
     * Check for expiring subscriptions every day at 9:00 AM
     * Send notification 7 days before expiry
     */
    @Scheduled(cron = "0 0 9 * * *") // 9 AM every day
    public void checkExpiringSubscriptions() {
        log.info("Checking for expiring subscriptions...");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysLater = now.plusDays(7);

        List<User> users = userRepository.findAll();

        for (User user : users) {
            if (user.getRole() == UserRole.PREMIUM && user.getPremiumExpiresAt() != null) {
                LocalDateTime expiresAt = user.getPremiumExpiresAt();

                // Check if expires in 7 days
                if (expiresAt.isAfter(now) && expiresAt.isBefore(sevenDaysLater)) {
                    long daysLeft = ChronoUnit.DAYS.between(now, expiresAt);

                    log.info("Sending expiring notification to user: {} ({}days left)",
                            user.getEmail(), daysLeft);

                    emailService.sendSubscriptionExpiringEmail(user, (int) daysLeft);
                }
            }
        }

        log.info("Expiring subscriptions check completed");
    }

    /**
     * Check for expired subscriptions every day at 0:00 AM
     * Downgrade users and send notification
     */
    @Scheduled(cron = "0 0 0 * * *") // Midnight every day
    public void checkExpiredSubscriptions() {
        log.info("Checking for expired subscriptions...");

        LocalDateTime now = LocalDateTime.now();
        List<User> users = userRepository.findAll();

        int expiredCount = 0;

        for (User user : users) {
            if (user.getRole() == UserRole.PREMIUM && user.getPremiumExpiresAt() != null) {
                if (user.getPremiumExpiresAt().isBefore(now)) {
                    log.info("Downgrading expired user: {}", user.getEmail());

                    // Downgrade to FREE
                    user.setRole(UserRole.FREE);
                    user.setPremiumExpiresAt(null);
                    userRepository.save(user);

                    // Send notification
                    emailService.sendSubscriptionExpiredEmail(user);

                    expiredCount++;
                }
            }
        }

        log.info("Expired subscriptions check completed. {} users downgraded", expiredCount);
    }

    /**
     * Send monthly summary email to admin
     * Every 1st day of month at 8:00 AM
     */
    @Scheduled(cron = "0 0 8 1 * *") // 8 AM on 1st day of each month
    public void sendMonthlySummary() {
        log.info("Generating monthly summary...");

        // TODO: Generate and send monthly summary to admins
        // - New users
        // - Revenue
        // - Active users
        // - Popular queries

        log.info("Monthly summary sent");
    }

    /**
     * Retry failed emails
     * Every 30 minutes
     */
    @Scheduled(fixedRate = 1800000) // 30 minutes
    public void retryFailedEmails() {
        log.info("Retrying failed emails...");

        // TODO: Implement email retry logic
        // - Get failed emails with retryCount < 3
        // - Resend
        // - Update retry count

        log.info("Failed emails retry completed");
    }

    /**
     * Clean up old data
     * Every Sunday at 2:00 AM
     */
    @Scheduled(cron = "0 0 2 * * SUN") // 2 AM every Sunday
    public void cleanupOldData() {
        log.info("Cleaning up old data...");

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        // TODO: Cleanup tasks:
        // - Delete old guest sessions
        // - Archive old email logs
        // - Clean up expired password reset tokens

        log.info("Cleanup completed");
    }
}
