package com.example.backend.service.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    @Value("${openai.model:deepseek-chat}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Gọi API AI (DeepSeek / OpenAI)
     */
    public String chat(String systemPrompt, String userMessage, List<Map<String, String>> history) {
        if (history == null) {
            history = new ArrayList<>();
        }

        systemPrompt = """
                Bạn là trợ lý AI thông minh của Tập đoàn Hòa Phát – một tập đoàn hàng đầu Việt Nam trong các lĩnh vực như thép, nông nghiệp, bất động sản và thiết bị gia dụng.
                
                Nhiệm vụ của bạn:
                - Trả lời chính xác, ngắn gọn và thân thiện về thông tin của Tập đoàn Hòa Phát.
                - Nếu người dùng hỏi về sản phẩm, lĩnh vực, lịch sử, lãnh đạo hoặc dự án của Hòa Phát, hãy trả lời chi tiết và chuyên nghiệp.
                - Nếu người dùng hỏi về vấn đề không thuộc Hòa Phát (ví dụ: chính trị, công nghệ ngoài công ty, các chủ đề không liên quan), hãy lịch sự chuyển hướng: 
                  "Xin lỗi, tôi chỉ có thể cung cấp thông tin liên quan đến Tập đoàn Hòa Phát và các hoạt động của công ty."
                - Khi có thể, hãy trích dẫn nguồn từ website chính thức (https://hoaphat.com.vn) hoặc báo cáo thường niên của Tập đoàn Hòa Phát.
                
                Phong cách trả lời:
                - Giọng điệu chuyên nghiệp, dễ hiểu, thân thiện như một nhân viên tư vấn khách hàng.
                - Nếu có thể, thêm ví dụ hoặc dữ kiện minh họa (ví dụ: “Hòa Phát hiện là nhà sản xuất thép lớn nhất Việt Nam, chiếm hơn 30% thị phần.”)
                """;

        // Thêm system message đầu tiên
        history.add(0, Map.of("role", "system", "content", systemPrompt));
        history.add(Map.of("role", "user", "content", userMessage));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", history,
                "temperature", 0.7,
                "max_tokens", 500
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        int maxRetries = 3;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, Map.class);

                if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                    return "⚠️ API không phản hồi đúng định dạng.";
                }

                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                if (choices == null || choices.isEmpty()) {
                    return "⚠️ API không có kết quả trả về.";
                }

                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                return message != null ? (String) message.get("content") : "⚠️ API không có nội dung.";

            } catch (HttpClientErrorException.TooManyRequests e) {
                log.warn("DeepSeek bị giới hạn tốc độ (429): thử lại lần {}/{}", attempt, maxRetries);
                sleep(attempt * 2000L);

            } catch (HttpClientErrorException e) {
                String errorBody = e.getResponseBodyAsString();
                log.error("DeepSeek API HTTP error: {}", errorBody);

                if (errorBody.contains("Insufficient Balance") || errorBody.contains("insufficient_quota")) {
                    return "💰 Hết hạn mức API hoặc tài khoản không đủ số dư.";
                } else if (errorBody.contains("invalid_api_key")) {
                    return "🚫 API key không hợp lệ. Vui lòng kiểm tra cấu hình.";
                }

                return "❌ Lỗi từ DeepSeek API: " + errorBody;

            } catch (Exception e) {
                log.error("Lỗi khi gọi DeepSeek API (attempt {}/{}): {}", attempt, maxRetries, e.getMessage());
                if (attempt == maxRetries) {
                    return "❌ Lỗi hệ thống khi gọi AI: " + e.getMessage();
                }
                sleep(attempt * 1000L);
            }
        }

        return "⚠️ Hệ thống AI đang quá tải, vui lòng thử lại sau.";
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }
}
