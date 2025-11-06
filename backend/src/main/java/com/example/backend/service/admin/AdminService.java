package com.example.backend.service.admin;

import com.example.backend.dto.response.*;
import com.example.backend.model.chat.ChatSession;
import com.example.backend.model.knowledge.HoaphatKnowledge;
import com.example.backend.model.user.User;
import com.example.backend.model.user.UserRole;
import com.example.backend.repository.ChatSessionRepository;
import com.example.backend.repository.HoaphatKnowledgeRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ChatSessionRepository sessionRepository;
    private final HoaphatKnowledgeRepository knowledgeRepository;
    private final MongoTemplate mongoTemplate;

    public DashboardStats getDashboardStats() {
        DashboardStats stats = new DashboardStats();

        // User statistics
        long totalUsers = userRepository.count();
        stats.setTotalUsers(totalUsers);

        // Count by role
        stats.setGuestCount(countUsersByRole(UserRole.FREE)); // Guest converted to free
        stats.setFreeCount(countUsersByRole(UserRole.FREE));
        stats.setPremiumCount(countUsersByRole(UserRole.PREMIUM));

        // Active users (last 24 hours)
        LocalDateTime yesterday = LocalDateTime.now().minus(24, ChronoUnit.HOURS);
        Query activeQuery = new Query(Criteria.where("lastLogin").gte(yesterday));
        stats.setActiveUsers(mongoTemplate.count(activeQuery, User.class));

        // Message statistics
        long totalMessages = sessionRepository.findAll().stream()
                .mapToLong(session -> session.getMessages() != null ? session.getMessages().size() : 0)
                .sum();
        stats.setTotalMessages(totalMessages);

        // Today's messages
        LocalDateTime todayStart = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
        Query todayQuery = new Query(Criteria.where("updatedAt").gte(todayStart));
        long todayMessages = sessionRepository.findAll().stream()
                .filter(s -> s.getUpdatedAt().isAfter(todayStart))
                .mapToLong(s -> s.getMessages() != null ? s.getMessages().size() : 0)
                .sum();
        stats.setTodayMessages(todayMessages);

        // Conversion rates
        stats.setConversionRate(calculateConversionRate());
        stats.setUpgradRate(calculateUpgradeRate());

        // Revenue (mock - integrate with payment system)
        stats.setTotalRevenue(calculateTotalRevenue());
        stats.setMonthlyRevenue(calculateMonthlyRevenue());

        // Recent activities
        stats.setRecentActivities(getRecentActivities(10));

        // Popular queries
        stats.setPopularQueries(getPopularQueries(10));

        // User growth data
        stats.setUserGrowthData(getUserGrowthData(30));

        return stats;
    }

    public List<UserManagementDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).stream()
                .map(this::convertToUserManagementDTO)
                .collect(Collectors.toList());
    }

    public UserManagementDTO getUserDetails(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return convertToUserManagementDTO(user);
    }

    public void toggleUserStatus(String userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setEnabled(enabled);
        userRepository.save(user);
    }

    public void updateUserRole(String userId, UserRole newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setRole(newRole);

        if (newRole == UserRole.PREMIUM) {
            user.setPremiumExpiresAt(LocalDateTime.now().plusYears(1));
        }

        userRepository.save(user);
    }

    public void deleteUser(String userId) {
        // Soft delete - disable user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setEnabled(false);
        userRepository.save(user);

        // Or hard delete
        // userRepository.deleteById(userId);
    }

    // Knowledge Base Management
    public List<KnowledgeManagementDTO> getAllKnowledge(Pageable pageable) {
        return knowledgeRepository.findAll(pageable).stream()
                .map(this::convertToKnowledgeDTO)
                .collect(Collectors.toList());
    }

    public KnowledgeManagementDTO createKnowledge(HoaphatKnowledge knowledge) {
        knowledge.setCreatedAt(LocalDateTime.now());
        knowledge.setUpdatedAt(LocalDateTime.now());
        HoaphatKnowledge saved = knowledgeRepository.save(knowledge);
        return convertToKnowledgeDTO(saved);
    }

    public KnowledgeManagementDTO updateKnowledge(String id, HoaphatKnowledge knowledge) {
        HoaphatKnowledge existing = knowledgeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Knowledge not found"));

        existing.setTitle(knowledge.getTitle());
        existing.setContent(knowledge.getContent());
        existing.setCategory(knowledge.getCategory());
        existing.setTags(knowledge.getTags());
        existing.setUpdatedAt(LocalDateTime.now());

        HoaphatKnowledge saved = knowledgeRepository.save(existing);
        return convertToKnowledgeDTO(saved);
    }

    public void deleteKnowledge(String id) {
        knowledgeRepository.deleteById(id);
    }

    public List<KnowledgeManagementDTO> searchKnowledge(String keyword) {
        return knowledgeRepository.fullTextSearch(keyword).stream()
                .map(this::convertToKnowledgeDTO)
                .collect(Collectors.toList());
    }

    // Analytics
    public Map<String, Object> getAnalytics(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> analytics = new HashMap<>();

        Query dateQuery = new Query(
                Criteria.where("createdAt").gte(startDate).lte(endDate)
        );

        // User registrations over time
        List<User> users = mongoTemplate.find(dateQuery, User.class);
        analytics.put("newUsersByDay", groupUsersByDay(users));

        // Messages over time
        analytics.put("messagesByDay", getMessagesByDay(startDate, endDate));

        // User activity
        analytics.put("userActivity", getUserActivityStats());

        // Conversion funnel
        analytics.put("conversionFunnel", getConversionFunnel());

        return analytics;
    }

    public List<Map<String, Object>> getSystemHealth() {
        List<Map<String, Object>> health = new ArrayList<>();

        // Database health
        health.add(Map.of(
                "component", "MongoDB",
                "status", "healthy",
                "responseTime", "12ms",
                "connections", mongoTemplate.getDb().getName()
        ));

        // API health
        health.add(Map.of(
                "component", "OpenAI API",
                "status", "healthy",
                "avgResponseTime", "850ms"
        ));

        // Cache health
        health.add(Map.of(
                "component", "Cache",
                "status", "healthy",
                "hitRate", "87.5%"
        ));

        return health;
    }

    // Helper methods
    private long countUsersByRole(UserRole role) {
        Query query = new Query(Criteria.where("role").is(role));
        return mongoTemplate.count(query, User.class);
    }

    private double calculateConversionRate() {
        // Mock calculation - guest to free conversion
        return 23.5; // 23.5%
    }

    private double calculateUpgradeRate() {
        // Mock calculation - free to premium conversion
        long freeUsers = countUsersByRole(UserRole.FREE);
        long premiumUsers = countUsersByRole(UserRole.PREMIUM);

        if (freeUsers == 0) return 0;
        return (double) premiumUsers / freeUsers * 100;
    }

    private long calculateTotalRevenue() {
        // Mock - integrate with payment system
        long premiumCount = countUsersByRole(UserRole.PREMIUM);
        return premiumCount * 1990000; // Average yearly subscription
    }

    private long calculateMonthlyRevenue() {
        return calculateTotalRevenue() / 12;
    }

    private List<RecentActivity> getRecentActivities(int limit) {
        List<RecentActivity> activities = new ArrayList<>();

        // Get recent users
        List<User> recentUsers = userRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(limit)
                .collect(Collectors.toList());

        for (User user : recentUsers) {
            RecentActivity activity = new RecentActivity();
            activity.setType(user.getRole() == UserRole.PREMIUM ? "upgrade" : "new_user");
            activity.setDescription(user.getFullName() + " đã " +
                    (user.getRole() == UserRole.PREMIUM ? "nâng cấp Premium" : "đăng ký"));
            activity.setTimestamp(user.getCreatedAt());
            activity.setUserId(user.getId());
            activities.add(activity);
        }

        return activities;
    }

    private List<PopularQuery> getPopularQueries(int limit) {
        // Analyze chat sessions for popular queries
        List<PopularQuery> queries = new ArrayList<>();

        // Mock data - in real implementation, analyze actual queries
        queries.add(createPopularQuery("Doanh thu Hòa Phát", 245, 4.5));
        queries.add(createPopularQuery("Sản phẩm thép", 189, 4.2));
        queries.add(createPopularQuery("Báo cáo tài chính", 156, 4.7));
        queries.add(createPopularQuery("Giá cổ phiếu HPG", 134, 4.3));
        queries.add(createPopularQuery("Dự án Dung Quất", 98, 4.6));

        return queries.stream().limit(limit).collect(Collectors.toList());
    }

    private PopularQuery createPopularQuery(String query, long count, double satisfaction) {
        PopularQuery pq = new PopularQuery();
        pq.setQuery(query);
        pq.setCount(count);
        pq.setAvgSatisfaction(satisfaction);
        return pq;
    }

    private List<UserGrowth> getUserGrowthData(int days) {
        List<UserGrowth> growthData = new ArrayList<>();

        for (int i = days; i >= 0; i--) {
            LocalDateTime date = LocalDateTime.now().minusDays(i);
            UserGrowth growth = new UserGrowth();
            growth.setDate(date.toLocalDate().toString());
            growth.setGuestUsers(i * 5L); // Mock data
            growth.setFreeUsers(i * 3L);
            growth.setPremiumUsers(i * 1L);
            growthData.add(growth);
        }

        return growthData;
    }

    private UserManagementDTO convertToUserManagementDTO(User user) {
        UserManagementDTO dto = new UserManagementDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setRole(user.getRole().name());
        dto.setEnabled(user.isEnabled());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setLastLogin(user.getLastLogin());
        dto.setPremiumExpiresAt(user.getPremiumExpiresAt());

        // Count messages
        List<ChatSession> sessions = sessionRepository.findByUserId(user.getId());
        long totalMessages = sessions.stream()
                .mapToLong(s -> s.getMessages() != null ? s.getMessages().size() : 0)
                .sum();
        dto.setTotalMessages(totalMessages);

        return dto;
    }

    private KnowledgeManagementDTO convertToKnowledgeDTO(HoaphatKnowledge knowledge) {
        KnowledgeManagementDTO dto = new KnowledgeManagementDTO();
        dto.setId(knowledge.getId());
        dto.setCategory(knowledge.getCategory());
        dto.setTitle(knowledge.getTitle());
        dto.setContent(knowledge.getContent());
        dto.setTags(knowledge.getTags());
        dto.setPremium(List.of("financial", "detailed_analysis", "internal_docs")
                .contains(knowledge.getCategory()));
        dto.setLastUpdated(knowledge.getUpdatedAt());
        // Mock usage stats
        dto.setViewCount((long) (Math.random() * 1000));
        dto.setUsageCount((long) (Math.random() * 500));
        return dto;
    }

    private Map<String, Long> groupUsersByDay(List<User> users) {
        return users.stream()
                .collect(Collectors.groupingBy(
                        u -> u.getCreatedAt().toLocalDate().toString(),
                        Collectors.counting()
                ));
    }

    private Map<String, Long> getMessagesByDay(LocalDateTime start, LocalDateTime end) {
        // Mock implementation
        Map<String, Long> messagesByDay = new HashMap<>();
        LocalDateTime current = start;

        while (current.isBefore(end)) {
            messagesByDay.put(
                    current.toLocalDate().toString(),
                    (long) (Math.random() * 1000)
            );
            current = current.plusDays(1);
        }

        return messagesByDay;
    }

    private Map<String, Object> getUserActivityStats() {
        return Map.of(
                "dailyActiveUsers", 456L,
                "weeklyActiveUsers", 1234L,
                "monthlyActiveUsers", 3456L,
                "avgSessionDuration", "8.5 minutes"
        );
    }

    private Map<String, Object> getConversionFunnel() {
        return Map.of(
                "visitors", 10000L,
                "guestUsers", 2500L,
                "registeredUsers", 875L,
                "premiumUsers", 156L
        );
    }
}
