package com.example.backend.controller;

import com.example.backend.dto.request.LoginRequest;
import com.example.backend.dto.request.RegisterRequest;
import com.example.backend.dto.response.AuthResponse;
import com.example.backend.dto.response.UserInfo;
import com.example.backend.model.user.PasswordResetToken;
import com.example.backend.model.user.User;
import com.example.backend.repository.PasswordResetTokenRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.auth.AuthService;
import com.example.backend.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository tokenRepository;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/upgrade-premium")
    public ResponseEntity<AuthResponse> upgradeToPremium(
            @RequestParam String plan,
            Authentication authentication
    ) {
        String userId = ((User) authentication.getPrincipal()).getId();
        AuthResponse response = authService.upgradeToPremium(userId, plan);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserInfo> getCurrentUser(Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        UserInfo userInfo = new UserInfo();
        userInfo.setId(user.getId());
        userInfo.setEmail(user.getEmail());
        userInfo.setFullName(user.getFullName());
        userInfo.setRole(user.getRole().name());
        userInfo.setPremium(user.isPremium());
        userInfo.setPremiumExpiresAt(user.getPremiumExpiresAt());

        return ResponseEntity.ok(userInfo);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @RequestBody Map<String, String> request) {

        String email = request.get("email");
        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null) {
            emailService.sendPasswordResetEmail(user);
        }

        // Always return success to prevent email enumeration
        return ResponseEntity.ok(Map.of(
                "message", "Nếu email tồn tại, chúng tôi đã gửi link đặt lại mật khẩu"
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(
            @RequestBody Map<String, String> request) {

        String token = request.get("token");
        String newPassword = request.get("newPassword");

        if (!emailService.verifyResetToken(token)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Token không hợp lệ hoặc đã hết hạn"
            ));
        }

        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token not found"));

        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        emailService.markTokenAsUsed(token);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đặt lại mật khẩu thành công"
        ));
    }

    @GetMapping("/verify-reset-token")
    public ResponseEntity<Map<String, Boolean>> verifyToken(
            @RequestParam String token) {

        boolean valid = emailService.verifyResetToken(token);
        return ResponseEntity.ok(Map.of("valid", valid));
    }
}
