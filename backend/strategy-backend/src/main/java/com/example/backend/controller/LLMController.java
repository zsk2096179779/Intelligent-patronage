package com.example.backend.controller;

import com.example.backend.service.DeepSeekService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/llm")
public class LLMController {

    private final DeepSeekService deepSeekService;

    public LLMController(DeepSeekService deepSeekService) {
        this.deepSeekService = deepSeekService;
    }

    @PostMapping("/query")
    public ResponseEntity<?> ask(@RequestBody QueryRequest request) {
        if (request == null || !StringUtils.hasText(request.getQuestion())) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", false);
            resp.put("message", "question 不能为空");
            return ResponseEntity.badRequest().body(resp);
        }

        try {
            String answer = deepSeekService.ask(request.getQuestion().trim());
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("answer", answer);
            return ResponseEntity.ok(resp);
        } catch (Exception ex) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", false);
            resp.put("message", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(resp);
        }
    }

    public static class QueryRequest {
        private String question;

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }
    }
}

