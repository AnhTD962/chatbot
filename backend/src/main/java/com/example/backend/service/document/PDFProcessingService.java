package com.example.backend.service.document;

import com.example.backend.model.document.DocumentProcessResult;
import com.example.backend.model.document.ProcessedDocument;
import com.example.backend.repository.DocumentRepository;
import com.example.backend.repository.HoaphatKnowledgeRepository;
import com.example.backend.service.chat.AIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class PDFProcessingService {

    private final DocumentRepository documentRepository;
    private final HoaphatKnowledgeRepository knowledgeRepository;
    private final AIService aiService;

    // Maximum file size: 50MB
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;

    public DocumentProcessResult processPDF(MultipartFile file, String userId) throws IOException {
        // Validate file
        validatePDFFile(file);

        // Extract text from PDF
        String extractedText = extractTextFromPDF(file);

        // Parse and analyze content
        Map<String, Object> analysis = analyzeContent(extractedText);

        // Save to database
        ProcessedDocument document = saveDocument(file, extractedText, analysis, userId);

        // Generate AI summary
        String summary = generateSummary(extractedText);

        DocumentProcessResult result = new DocumentProcessResult();
        result.setDocumentId(document.getId());
        result.setFileName(file.getOriginalFilename());
        result.setPageCount(document.getPageCount());
        result.setWordCount(document.getWordCount());
        result.setExtractedText(extractedText);
        result.setSummary(summary);
        result.setKeywords(document.getKeywords());
        result.setEntities(document.getEntities());
        result.setTables(document.getTables());

        return result;
    }

    private void validatePDFFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("File size exceeds limit (50MB)");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".pdf")) {
            throw new RuntimeException("File must be PDF format");
        }
    }

    private String extractTextFromPDF(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            stripper.setLineSeparator("\n");

            String text = stripper.getText(document).trim();

            // Nếu không có text (PDF scan), fallback sang OCR
            if (text.isEmpty()) {
                log.warn("PDF has no text layer — running OCR...");
                text = performOCROnPDF(file);
            }

            return text;
        }
    }

    private String performOCROnPDF(MultipartFile file) {
        StringBuilder result = new StringBuilder();
        int MAX_THREADS = Math.max(2, Runtime.getRuntime().availableProcessors() - 1); // Số luồng hợp lý
        ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);
        List<Future<String>> futures = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            int totalPages = document.getNumberOfPages();
            log.info("📄 OCR bắt đầu cho {} trang, sử dụng {} luồng...", totalPages, MAX_THREADS);

            for (int pageIndex = 0; pageIndex < totalPages; pageIndex++) {
                final int currentPage = pageIndex;
                futures.add(executor.submit(() -> {
                    try {
                        BufferedImage image = pdfRenderer.renderImageWithDPI(currentPage, 200);
                        if (isMostlyBlank(image)) {
                            log.info("⚪ Bỏ qua trang {} vì trống hoặc gần trống", currentPage + 1);
                            return "";
                        }

                        // Mỗi luồng có instance riêng của Tesseract
                        Tesseract tesseract = new Tesseract();
                        String tessDataPath = new File("src/main/resources/tessdata").getAbsolutePath();
                        tesseract.setDatapath(tessDataPath);
                        tesseract.setLanguage("vie+eng");
                        tesseract.setOcrEngineMode(1); // LSTM
                        tesseract.setPageSegMode(1);
                        tesseract.setTessVariable("load_system_dawg", "F");
                        tesseract.setTessVariable("load_freq_dawg", "F");

                        String pageText = tesseract.doOCR(image);
                        log.info("✅ OCR xong trang {}/{}", currentPage + 1, totalPages);
                        image.flush();
                        return pageText.trim();
                    } catch (Exception e) {
                        log.error("⚠️ Lỗi OCR tại trang {}: {}", currentPage + 1, e.getMessage());
                        return "";
                    }
                }));
            }

            // Gom kết quả theo thứ tự trang
            for (int i = 0; i < futures.size(); i++) {
                try {
                    String pageResult = futures.get(i).get(3, TimeUnit.MINUTES);
                    if (!pageResult.isEmpty()) {
                        result.append("=== Trang ").append(i + 1).append(" ===\n");
                        result.append(pageResult).append("\n\n");
                    }
                } catch (TimeoutException e) {
                    log.warn("⚠️ Hết thời gian OCR cho trang {}", i + 1);
                }
            }

        } catch (Exception e) {
            log.error("❌ Lỗi khi đọc PDF: {}", e.getMessage(), e);
        } finally {
            executor.shutdown();
        }

        long duration = (System.currentTimeMillis() - startTime) / 1000;
        log.info("🏁 OCR hoàn tất sau {} giây", duration);
        return result.toString().trim();
    }

    private boolean isMostlyBlank(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int stepX = width / 20;
        int stepY = height / 20;

        int darkPixels = 0;
        int totalPixels = 0;

        for (int y = 0; y < height; y += stepY) {
            for (int x = 0; x < width; x += stepX) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int brightness = (r + g + b) / 3;
                if (brightness < 240) darkPixels++;
                totalPixels++;
            }
        }

        double darkRatio = (double) darkPixels / totalPixels;
        return darkRatio < 0.02; // dưới 2% điểm ảnh tối coi như trang trống
    }

    private Map<String, Object> analyzeContent(String text) {
        Map<String, Object> analysis = new HashMap<>();

        // Count words
        int wordCount = text.split("\\s+").length;
        analysis.put("wordCount", wordCount);

        // Extract keywords
        List<String> keywords = extractKeywords(text);
        analysis.put("keywords", keywords);

        // Extract named entities (companies, people, locations)
        Map<String, List<String>> entities = extractEntities(text);
        analysis.put("entities", entities);

        // Extract numbers (revenue, percentages, etc.)
        List<Map<String, Object>> numbers = extractNumbers(text);
        analysis.put("numbers", numbers);

        // Extract tables (simple detection)
        List<String> tables = extractTables(text);
        analysis.put("tables", tables);

        // Extract dates
        List<String> dates = extractDates(text);
        analysis.put("dates", dates);

        return analysis;
    }

    private List<String> extractKeywords(String text) {
        // Simple keyword extraction (in production, use NLP library)
        List<String> keywords = new ArrayList<>();

        String[] commonKeywords = {
                "doanh thu", "lợi nhuận", "tăng trưởng", "sản xuất", "thép",
                "đầu tư", "dự án", "báo cáo", "tài chính", "kinh doanh",
                "xuất khẩu", "nhập khẩu", "giá", "thị trường", "chiến lược"
        };

        String lowerText = text.toLowerCase();
        for (String keyword : commonKeywords) {
            if (lowerText.contains(keyword)) {
                keywords.add(keyword);
            }
        }

        return keywords;
    }

    private Map<String, List<String>> extractEntities(String text) {
        Map<String, List<String>> entities = new HashMap<>();

        // Extract companies (simple pattern matching)
        List<String> companies = new ArrayList<>();
        Pattern companyPattern = Pattern.compile("(Công ty|Tập đoàn|TNHH)\\s+([A-ZẮẰẲẴẶẤẦẨẪẬĐẾỀỂỄỆÍÌỈĨỊÓÒỎÕỌỐỒỔỖỘỚỜỞỠỢÚÙỦŨỤỨỪỬỮỰÝỲỶỸỴ][a-zắằẳẵặấầẩẫậđếềểễệíìỉĩịóòỏõọốồổỗộớờởỡợúùủũụứừửữựýỳỷỹỵA-ZẮẰẲẴẶẤẦẨẪẬĐẾỀỂỄỆÍÌỈĨỊÓÒỎÕỌỐỒỔỖỘỚỜỞỠỢÚÙỦŨỤỨỪỬỮỰÝỲỶỸỴ\\s]+)");
        Matcher companyMatcher = companyPattern.matcher(text);
        while (companyMatcher.find()) {
            companies.add(companyMatcher.group(0));
        }
        entities.put("companies", companies);

        // Extract people (simple pattern)
        List<String> people = new ArrayList<>();
        Pattern peoplePattern = Pattern.compile("(Ông|Bà|Anh|Chị)\\s+([A-ZẮẰẲẴẶẤẦẨẪẬĐẾỀỂỄỆÍÌỈĨỊÓÒỎÕỌỐỒỔỖỘỚỜỞỠỢÚÙỦŨỤỨỪỬỮỰÝỲỶỸỴ][a-zắằẳẵặấầẩẫậđếềểễệíìỉĩịóòỏõọốồổỗộớờởỡợúùủũụứừửữựýỳỷỹỵ]+(?:\\s+[A-ZẮẰẲẴẶẤẦẨẪẬĐẾỀỂỄỆÍÌỈĨỊÓÒỎÕỌỐỒỔỖỘỚỜỞỠỢÚÙỦŨỤỨỪỬỮỰÝỲỶỸỴ][a-zắằẳẵặấầẩẫậđếềểễệíìỉĩịóòỏõọốồổỗộớờởỡợúùủũụứừửữựýỳỷỹỵ]+){1,2})");
        Matcher peopleMatcher = peoplePattern.matcher(text);
        while (peopleMatcher.find()) {
            people.add(peopleMatcher.group(0));
        }
        entities.put("people", people);

        // Extract locations
        List<String> locations = new ArrayList<>();
        String[] vietnamProvinces = {
                "Hà Nội", "TP Hồ Chí Minh", "Đà Nẵng", "Hải Phòng",
                "Quảng Ngãi", "Thanh Hóa", "Nghệ An", "Bắc Giang"
        };
        for (String province : vietnamProvinces) {
            if (text.contains(province)) {
                locations.add(province);
            }
        }
        entities.put("locations", locations);

        return entities;
    }

    private List<Map<String, Object>> extractNumbers(String text) {
        List<Map<String, Object>> numbers = new ArrayList<>();

        // Extract revenue numbers
        Pattern revenuePattern = Pattern.compile("(\\d+[.,]?\\d*)\\s*(tỷ|triệu|nghìn)\\s*(đồng|VND)?");
        Matcher revenueMatcher = revenuePattern.matcher(text);
        while (revenueMatcher.find()) {
            Map<String, Object> numberInfo = new HashMap<>();
            numberInfo.put("value", revenueMatcher.group(1));
            numberInfo.put("unit", revenueMatcher.group(2));
            numberInfo.put("type", "currency");
            numbers.add(numberInfo);
        }

        // Extract percentages
        Pattern percentPattern = Pattern.compile("(\\d+[.,]?\\d*)\\s*%");
        Matcher percentMatcher = percentPattern.matcher(text);
        while (percentMatcher.find()) {
            Map<String, Object> numberInfo = new HashMap<>();
            numberInfo.put("value", percentMatcher.group(1));
            numberInfo.put("unit", "%");
            numberInfo.put("type", "percentage");
            numbers.add(numberInfo);
        }

        return numbers;
    }

    private List<String> extractTables(String text) {
        List<String> tables = new ArrayList<>();

        // Simple table detection (lines with multiple tabs or consistent spacing)
        String[] lines = text.split("\n");
        List<String> currentTable = new ArrayList<>();

        for (String line : lines) {
            if (line.contains("\t") || line.matches(".*\\s{3,}.*")) {
                currentTable.add(line);
            } else if (!currentTable.isEmpty()) {
                if (currentTable.size() >= 3) { // At least 3 rows
                    tables.add(String.join("\n", currentTable));
                }
                currentTable.clear();
            }
        }

        return tables;
    }

    private List<String> extractDates(String text) {
        List<String> dates = new ArrayList<>();

        // Extract dates in various formats
        Pattern datePattern = Pattern.compile("\\b(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}|\\d{4}[/-]\\d{1,2}[/-]\\d{1,2}|" +
                "(ngày|tháng)\\s*\\d{1,2}\\s*(tháng|năm)\\s*\\d{1,2}(\\s*năm\\s*\\d{2,4})?)\\b");
        Matcher dateMatcher = datePattern.matcher(text);
        while (dateMatcher.find()) {
            dates.add(dateMatcher.group(0));
        }

        return dates;
    }

    private ProcessedDocument saveDocument(MultipartFile file, String text,
                                           Map<String, Object> analysis, String userId) {
        ProcessedDocument document = new ProcessedDocument();
        document.setFileName(file.getOriginalFilename());
        document.setFileSize(file.getSize());
        document.setUserId(userId);
        document.setExtractedText(text);
        document.setPageCount(calculatePageCount(text));
        document.setWordCount((Integer) analysis.get("wordCount"));
        document.setKeywords((List<String>) analysis.get("keywords"));
        document.setEntities((Map<String, List<String>>) analysis.get("entities"));
        document.setNumbers((List<Map<String, Object>>) analysis.get("numbers"));
        document.setTables((List<String>) analysis.get("tables"));
        document.setDates((List<String>) analysis.get("dates"));
        document.setProcessedAt(LocalDateTime.now());

        return documentRepository.save(document);
    }

    private int calculatePageCount(String text) {
        // Rough estimation: ~500 words per page
        int wordCount = text.split("\\s+").length;
        return Math.max(1, wordCount / 500);
    }

    private String generateSummary(String text) {
        if (text == null || text.isBlank()) {
            return "Không thể tóm tắt vì tài liệu rỗng hoặc không đọc được nội dung.";
        }

        // Cắt bớt nội dung nếu quá dài (AI thường giới hạn token)
        String truncatedText = text.length() > 8000 ? text.substring(0, 8000) : text;

        String systemPrompt = """
                Bạn là trợ lý AI chuyên tóm tắt tài liệu của Tập đoàn Hòa Phát.
                Hãy đọc nội dung tài liệu sau và tóm tắt ngắn gọn, có cấu trúc rõ ràng:
                - Trình bày theo gạch đầu dòng
                - Nêu các điểm chính, số liệu, dự án, kết quả tài chính, và thông tin nổi bật
                - Tránh sao chép nguyên văn, viết lại nội dung tự nhiên, dễ hiểu.
                """;

        String userMessage = "Dưới đây là nội dung tài liệu cần tóm tắt:\n\n" + truncatedText;

        try {
            String summary = aiService.chat(systemPrompt, userMessage, null);
            return summary != null && !summary.isBlank()
                    ? summary
                    : "Không thể tạo tóm tắt bằng AI (kết quả rỗng).";
        } catch (Exception e) {
            log.error("Lỗi khi tóm tắt tài liệu bằng AI: {}", e.getMessage());
            return "Không thể tóm tắt tài liệu bằng AI: " + e.getMessage();
        }
    }

    public String askQuestionAboutDocument(String documentId, String question) {
        ProcessedDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        String context = document.getExtractedText();

        // Giới hạn nội dung gửi đi (để tránh vượt quá context limit)
        int maxLength = 15000; // tùy theo model, nên < 15k tokens
        if (context.length() > maxLength) {
            context = context.substring(0, maxLength) + "\n\n...[Tài liệu đã được rút gọn để xử lý]...";
        }

        String systemPrompt = """
            Bạn là trợ lý AI hiểu rõ tài liệu sau đây.
            Hãy trả lời ngắn gọn và chính xác dựa trên nội dung tài liệu.
            """;

        String userMessage = "Câu hỏi: " + question + "\n\nTài liệu:\n" + context;

        try {
            return aiService.chat(systemPrompt, userMessage, null);
        } catch (Exception e) {
            log.error("AIService error: {}", e.getMessage());
            return "Xin lỗi, tài liệu quá dài hoặc có lỗi trong quá trình xử lý.";
        }
    }


    public List<ProcessedDocument> getUserDocuments(String userId) {
        return documentRepository.findByUserId(userId);
    }

    public ProcessedDocument getUserDocument(String documentId, String userId) {
        ProcessedDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        if (!doc.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        return doc;
    }

    public void deleteDocument(String documentId, String userId) {
        ProcessedDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (!document.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        documentRepository.delete(document);
    }
}
