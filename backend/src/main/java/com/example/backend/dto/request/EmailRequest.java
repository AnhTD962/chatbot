package com.example.backend.dto.request;

import com.example.backend.model.email.EmailType;
import lombok.Data;
import java.util.Map;

@Data
public class EmailRequest {
    private String to;
    private String subject;
    private String templateName;
    private Map<String, Object> variables;
    private EmailType emailType;
}