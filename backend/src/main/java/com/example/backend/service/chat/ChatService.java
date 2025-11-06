package com.example.backend.service.chat;

import com.example.backend.dto.request.ChatRequest;
import com.example.backend.dto.response.ChatResponse;
import com.example.backend.model.chat.ChatSession;
import com.example.backend.model.knowledge.HoaphatKnowledge;
import com.example.backend.model.chat.Message;
import com.example.backend.repository.ChatSessionRepository;
import com.example.backend.repository.HoaphatKnowledgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatSessionRepository sessionRepository;
    private final HoaphatKnowledgeRepository knowledgeRepository;
    private final AIService aiService;

    public ChatResponse chat(ChatRequest request) {
        ChatSession session = getOrCreateSession(request.getSessionId(), request.getUserId());

        List<HoaphatKnowledge> relevantKnowledge = searchRelevantKnowledge(request.getMessage());
        String context = buildContext(relevantKnowledge);
        String systemPrompt = buildSystemPrompt(context);

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
        saveMessages(session, request.getMessage(), aiResponse);

        ChatResponse response = new ChatResponse();
        response.setSessionId(session.getId());
        response.setResponse(aiResponse);
        response.setTimestamp(LocalDateTime.now());
        response.setSources(relevantKnowledge.stream()
                .map(HoaphatKnowledge::getTitle)
                .collect(Collectors.toList()));

        return response;
    }

    private ChatSession getOrCreateSession(String sessionId, String userId) {
        if (sessionId != null && !sessionId.isEmpty()) {
            return sessionRepository.findById(sessionId)
                    .orElseGet(() -> createNewSession(userId));
        }
        return createNewSession(userId);
    }

    private ChatSession createNewSession(String userId) {
        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        session.setMessages(new ArrayList<>());
        return sessionRepository.save(session);
    }

    private List<HoaphatKnowledge> searchRelevantKnowledge(String query) {
        List<HoaphatKnowledge> results = knowledgeRepository.fullTextSearch(query);
        return results.stream().limit(5).collect(Collectors.toList());
    }

    private String buildContext(List<HoaphatKnowledge> knowledge) {
        if (knowledge.isEmpty()) {
            return "Không tìm thấy thông tin cụ thể trong cơ sở dữ liệu.";
        }

        StringBuilder context = new StringBuilder("Thông tin từ cơ sở dữ liệu Hòa Phát:\n\n");
        for (HoaphatKnowledge k : knowledge) {
            context.append("- ").append(k.getTitle()).append(": ")
                    .append(k.getContent()).append("\n\n");
        }
        return context.toString();
    }

    private String buildSystemPrompt(String context) {
        return "Bạn là trợ lý AI chuyên về Tập đoàn Hòa Phát. " +
                "Nhiệm vụ của bạn là trả lời các câu hỏi về Hòa Phát dựa trên thông tin được cung cấp. " +
                "Hãy trả lời bằng tiếng Việt, chính xác, chuyên nghiệp và thân thiện.\n\n" +
                context;
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

    public List<ChatSession> getUserSessions(String userId) {
        return sessionRepository.findByUserId(userId);
    }
}