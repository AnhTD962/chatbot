package com.example.backend.controller;

import com.example.backend.model.document.DocumentProcessResult;
import com.example.backend.model.document.ProcessedDocument;
import com.example.backend.model.user.User;
import com.example.backend.service.document.PDFProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class DocumentController {

    private final PDFProcessingService pdfService;

    @PostMapping("/upload")
    public ResponseEntity<DocumentProcessResult> uploadPDF(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        try {
            User user = (User) authentication.getPrincipal();
            DocumentProcessResult result = pdfService.processPDF(file, user.getId());
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<ProcessedDocument>> getMyDocuments(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<ProcessedDocument> documents = pdfService.getUserDocuments(user.getId());
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<ProcessedDocument> getDocument(
            @PathVariable String documentId,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        ProcessedDocument doc = pdfService.getUserDocument(documentId, user.getId());
        return ResponseEntity.ok(doc);
    }

    @PostMapping("/{documentId}/ask")
    public ResponseEntity<Map<String, String>> askQuestion(
            @PathVariable String documentId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        String question = request.get("question");
        String answer = pdfService.askQuestionAboutDocument(documentId, question);

        return ResponseEntity.ok(Map.of("answer", answer));
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable String documentId,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();
        pdfService.deleteDocument(documentId, user.getId());
        return ResponseEntity.ok().build();
    }
}

