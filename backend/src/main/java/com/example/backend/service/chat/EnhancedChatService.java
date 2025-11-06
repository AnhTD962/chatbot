package com.example.backend.service.chat;

import com.example.backend.dto.request.ChatRequest;
import com.example.backend.dto.response.ChatResponse;
import com.example.backend.model.chat.ChatSession;
import com.example.backend.model.chat.Message;
import com.example.backend.model.knowledge.HoaphatKnowledge;
import com.example.backend.model.user.GuestSession;
import com.example.backend.model.user.User;
import com.example.backend.model.user.UserRole;
import com.example.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnhancedChatService {

    private final ChatSessionRepository sessionRepository;
    private final HoaphatKnowledgeRepository knowledgeRepository;
    private final GuestSessionRepository guestSessionRepository;
    private final UserRepository userRepository;
    private final AIService aiService;

    private static final int GUEST_QUESTION_LIMIT = 5;
    private static final List<String> PREMIUM_CATEGORIES = List.of("financial", "detailed_analysis", "internal_docs");

    public ChatResponse chat(ChatRequest request, String authenticatedUserEmail) {
        boolean isGuest = (authenticatedUserEmail == null);
        User authenticatedUser = null;
        GuestSession guestSession = null;

        if (isGuest) {
            guestSession = handleGuestSession(request.getGuestFingerprint());
            if (guestSession.getQuestionCount() >= GUEST_QUESTION_LIMIT) {
                return createLimitReachedResponse();
            }
        } else {
            authenticatedUser = userRepository.findByEmail(authenticatedUserEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }

        boolean isPremium = isPremiumUser(authenticatedUser);

        if (request.isRequestDetailedInfo() && !isPremium) {
            return createPremiumRequiredResponse();
        }

        ChatSession session = getOrCreateSession(
                request.getSessionId(),
                isGuest ? "guest_" + guestSession.getId() : authenticatedUser.getId()
        );

        List<HoaphatKnowledge> relevantKnowledge = searchRelevantKnowledge(
                request.getMessage(), isPremium
        );

        if (!isPremium && containsPremiumContent(relevantKnowledge)) {
            relevantKnowledge = filterNonPremiumContent(relevantKnowledge);
        }

        String context = buildContext(relevantKnowledge, isPremium);
        String systemPrompt = buildSystemPrompt(context, isPremium);

        List<Map<String, String>> history = new ArrayList<>();
        if (session.getMessages() != null) {
            session.getMessages().stream()
                    .limit(10)
                    .forEach(msg -> history.add(Map.of(
                            "role", msg.getRole(),
                            "content", msg.getContent()
                    )));
        }

        String aiResponse = aiService.chat(systemPrompt, request.getMessage(), history);

        if (!isPremium && hasMoreDetailedInfo(request.getMessage())) {
            aiResponse += "\n\n💎 *Nâng cấp lên Premium để xem thông tin chi tiết hơn về tài chính, phân tích chuyên sâu và các tài liệu nội bộ.*";
        }

        saveMessages(session, request.getMessage(), aiResponse);

        if (isGuest) {
            guestSession.setQuestionCount(guestSession.getQuestionCount() + 1);
            guestSession.setLastUsed(LocalDateTime.now());
            guestSessionRepository.save(guestSession);
        }

        ChatResponse response = new ChatResponse();
        response.setSessionId(session.getId());
        response.setResponse(aiResponse);
        response.setTimestamp(LocalDateTime.now());
        response.setSources(relevantKnowledge.stream().map(HoaphatKnowledge::getTitle).collect(Collectors.toList()));

        if (isGuest) {
            response.setGuest(true);
            response.setRemainingQuestions(GUEST_QUESTION_LIMIT - guestSession.getQuestionCount());
        }

        return response;
    }

    // ==== Helper Methods ====

    private GuestSession handleGuestSession(String fingerprint) {
        return guestSessionRepository.findByFingerprint(fingerprint)
                .orElseGet(() -> {
                    GuestSession newSession = new GuestSession();
                    newSession.setFingerprint(fingerprint);
                    newSession.setQuestionCount(0);
                    newSession.setCreatedAt(LocalDateTime.now());
                    newSession.setLastUsed(LocalDateTime.now());
                    newSession.setExpiresAt(LocalDateTime.now().plusDays(1));
                    return guestSessionRepository.save(newSession);
                });
    }

    private boolean isPremiumUser(User user) {
        return user != null && (user.isPremium() || user.getRole() == UserRole.ADMIN);
    }

    private List<HoaphatKnowledge> searchRelevantKnowledge(String query, boolean includePremium) {
        List<HoaphatKnowledge> results = knowledgeRepository.fullTextSearch(query);
        if (!includePremium) {
            results = results.stream()
                    .filter(k -> !PREMIUM_CATEGORIES.contains(k.getCategory()))
                    .collect(Collectors.toList());
        }
        return results.stream().limit(includePremium ? 10 : 5).collect(Collectors.toList());
    }

    private boolean containsPremiumContent(List<HoaphatKnowledge> knowledge) {
        return knowledge.stream().anyMatch(k -> PREMIUM_CATEGORIES.contains(k.getCategory()));
    }

    private List<HoaphatKnowledge> filterNonPremiumContent(List<HoaphatKnowledge> knowledge) {
        return knowledge.stream()
                .filter(k -> !PREMIUM_CATEGORIES.contains(k.getCategory()))
                .collect(Collectors.toList());
    }

    private boolean hasMoreDetailedInfo(String query) {
        String lower = query.toLowerCase();
        return lower.contains("tài chính") || lower.contains("doanh thu")
                || lower.contains("lợi nhuận") || lower.contains("phân tích")
                || lower.contains("báo cáo");
    }

    private String buildContext(List<HoaphatKnowledge> knowledge, boolean isPremium) {
        if (knowledge.isEmpty()) return "Không tìm thấy thông tin cụ thể trong cơ sở dữ liệu.";
        StringBuilder sb = new StringBuilder("Thông tin từ cơ sở dữ liệu Hòa Phát:\n\n");
        for (HoaphatKnowledge k : knowledge) {
            sb.append("- ").append(k.getTitle()).append(": ");
            if (!isPremium && k.getContent().length() > 200)
                sb.append(k.getContent(), 0, 200).append("...");
            else
                sb.append(k.getContent());
            sb.append("\n\n");
        }
        return sb.toString();
    }

    private String buildSystemPrompt(String context, boolean isPremium) {
        String base = "Bạn là trợ lý AI chuyên về Tập đoàn Hòa Phát. " +
                "Trả lời chính xác, bằng tiếng Việt, thân thiện và chuyên nghiệp.\n\n";
        if (!isPremium) {
            base += "Người dùng là tài khoản miễn phí, chỉ cung cấp thông tin tổng quan. " +
                    "Với thông tin tài chính, phân tích sâu, hãy khuyến khích nâng cấp Premium.\n\n";
        }
        return base + context;
    }

    private ChatResponse createLimitReachedResponse() {
        ChatResponse r = new ChatResponse();
        r.setResponse("⚠️ Bạn đã hết 5 câu hỏi miễn phí!\n\n" +
                "Đăng ký tài khoản miễn phí để tiếp tục sử dụng chatbot hoặc nâng cấp Premium để xem thông tin tài chính chi tiết, phân tích chuyên sâu.");
        r.setLimitReached(true);
        r.setTimestamp(LocalDateTime.now());
        return r;
    }

    private ChatResponse createPremiumRequiredResponse() {
        ChatResponse r = new ChatResponse();
        r.setResponse("💎 Nội dung này chỉ dành cho tài khoản Premium.\n\n" +
                "Vui lòng nâng cấp để xem dữ liệu chi tiết về tài chính, báo cáo và phân tích sâu của Tập đoàn Hòa Phát.");
        r.setPremiumRequired(true);
        r.setTimestamp(LocalDateTime.now());
        return r;
    }

    private ChatSession getOrCreateSession(String sessionId, String userId) {
        if (sessionId != null && !sessionId.isEmpty()) {
            return sessionRepository.findById(sessionId)
                    .orElseGet(() -> createNewSession(userId));
        }
        return createNewSession(userId);
    }

    private ChatSession createNewSession(String userId) {
        ChatSession s = new ChatSession();
        s.setUserId(userId);
        s.setCreatedAt(LocalDateTime.now());
        s.setUpdatedAt(LocalDateTime.now());
        s.setMessages(new ArrayList<>());
        return sessionRepository.save(s);
    }

    private void saveMessages(ChatSession session, String userMessage, String aiResponse) {
        Message userMsg = new Message();
        userMsg.setRole("user");
        userMsg.setContent(userMessage);
        userMsg.setTimestamp(LocalDateTime.now());

        Message assistantMsg = new Message();
        assistantMsg.setRole("assistant");
        assistantMsg.setContent(aiResponse);
        assistantMsg.setTimestamp(LocalDateTime.now());

        session.getMessages().add(userMsg);
        session.getMessages().add(assistantMsg);
        session.setUpdatedAt(LocalDateTime.now());

        sessionRepository.save(session);
    }
}
