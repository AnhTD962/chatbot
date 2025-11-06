package com.example.backend.controller;

import com.example.backend.dto.response.DashboardStats;
import com.example.backend.dto.response.KnowledgeManagementDTO;
import com.example.backend.dto.response.UserManagementDTO;
import com.example.backend.model.knowledge.HoaphatKnowledge;
import com.example.backend.model.user.UserRole;
import com.example.backend.service.admin.AdminService;
import com.example.backend.websocket.WebSocketEventListener;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final WebSocketEventListener webSocketEventListener;

    // Dashboard
    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardStats> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/dashboard/realtime")
    public ResponseEntity<Map<String, Object>> getRealtimeStats() {
        return ResponseEntity.ok(Map.of(
                "activeConnections", webSocketEventListener.getActiveUserCount(),
                "activeSessions", webSocketEventListener.getActiveSessions(),
                "timestamp", LocalDateTime.now()
        ));
    }

    // User Management
    @GetMapping("/users")
    public ResponseEntity<List<UserManagementDTO>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllUsers(pageable));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserManagementDTO> getUserDetails(@PathVariable String userId) {
        return ResponseEntity.ok(adminService.getUserDetails(userId));
    }

    @PutMapping("/users/{userId}/status")
    public ResponseEntity<Void> toggleUserStatus(
            @PathVariable String userId,
            @RequestParam boolean enabled) {
        adminService.toggleUserStatus(userId, enabled);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<Void> updateUserRole(
            @PathVariable String userId,
            @RequestParam UserRole role) {
        adminService.updateUserRole(userId, role);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }

    // Knowledge Base Management
    @GetMapping("/knowledge")
    public ResponseEntity<List<KnowledgeManagementDTO>> getAllKnowledge(Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllKnowledge(pageable));
    }

    @PostMapping("/knowledge")
    public ResponseEntity<KnowledgeManagementDTO> createKnowledge(
            @RequestBody HoaphatKnowledge knowledge) {
        return ResponseEntity.ok(adminService.createKnowledge(knowledge));
    }

    @PutMapping("/knowledge/{id}")
    public ResponseEntity<KnowledgeManagementDTO> updateKnowledge(
            @PathVariable String id,
            @RequestBody HoaphatKnowledge knowledge) {
        return ResponseEntity.ok(adminService.updateKnowledge(id, knowledge));
    }

    @DeleteMapping("/knowledge/{id}")
    public ResponseEntity<Void> deleteKnowledge(@PathVariable String id) {
        adminService.deleteKnowledge(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/knowledge/search")
    public ResponseEntity<List<KnowledgeManagementDTO>> searchKnowledge(
            @RequestParam String keyword) {
        return ResponseEntity.ok(adminService.searchKnowledge(keyword));
    }

    // Analytics
    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getAnalytics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(adminService.getAnalytics(startDate, endDate));
    }

    @GetMapping("/analytics/export")
    public ResponseEntity<byte[]> exportAnalytics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        // TODO: Implement CSV/Excel export
        return ResponseEntity.ok().build();
    }

    // System Health
    @GetMapping("/system/health")
    public ResponseEntity<List<Map<String, Object>>> getSystemHealth() {
        return ResponseEntity.ok(adminService.getSystemHealth());
    }

    @GetMapping("/system/logs")
    public ResponseEntity<List<Map<String, Object>>> getSystemLogs(
            @RequestParam(defaultValue = "100") int limit) {
        // TODO: Implement log retrieval
        return ResponseEntity.ok(List.of());
    }

    // Bulk Operations
    @PostMapping("/users/bulk-update")
    public ResponseEntity<Void> bulkUpdateUsers(
            @RequestBody Map<String, Object> updateData) {
        // TODO: Implement bulk user updates
        return ResponseEntity.ok().build();
    }

    @PostMapping("/knowledge/bulk-import")
    public ResponseEntity<Map<String, Object>> bulkImportKnowledge(
            @RequestBody List<HoaphatKnowledge> knowledgeList) {
        // TODO: Implement bulk import
        return ResponseEntity.ok(Map.of(
                "imported", knowledgeList.size(),
                "success", true
        ));
    }
}
