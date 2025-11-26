package com.example.backend.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DeepSeekService {

    private final RestTemplate restTemplate;

    private static final String API_BASE_URL = "https://api.deepseek.com";
    private static final String DEFAULT_MODEL = "deepseek-chat";
    private static final String API_KEY = "sk-b5c1ecac44404c76820808e7d38adc59";

    public DeepSeekService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(60))
                .build();
    }

    public String ask(String question) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(API_KEY);

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", DEFAULT_MODEL);
        payload.put("temperature", 0.7);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(systemPrompt());
        messages.add(userPrompt(question));
        payload.put("messages", messages);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);
        String endpoint = ensureEndsWithoutSlash(API_BASE_URL) + "/chat/completions";

        ResponseEntity<DeepSeekResponse> response = restTemplate.postForEntity(
                endpoint,
                requestEntity,
                DeepSeekResponse.class
        );

        DeepSeekResponse body = response.getBody();
        if (body == null || CollectionUtils.isEmpty(body.getChoices())) {
            throw new IllegalStateException("大模型未返回有效内容");
        }

        DeepSeekMessage message = body.getChoices().get(0).getMessage();
        if (message == null || message.getContent() == null) {
            throw new IllegalStateException("大模型返回内容为空");
        }
        return message.getContent().trim();
    }

    private Map<String, String> systemPrompt() {
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "你是一名熟悉投资策略与量化管理的助手，请结合上下文用中文回答用户的问题。");
        return systemMessage;
    }

    private Map<String, String> userPrompt(String question) {
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", question);
        return userMessage;
    }

    private String ensureEndsWithoutSlash(String url) {
        if (url == null || url.isBlank()) {
            return "https://api.deepseek.com";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DeepSeekResponse {
        private List<Choice> choices;
        private DeepSeekError error;

        public List<Choice> getChoices() {
            return choices;
        }

        public void setChoices(List<Choice> choices) {
            this.choices = choices;
        }

        public DeepSeekError getError() {
            return error;
        }

        public void setError(DeepSeekError error) {
            this.error = error;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {
        private DeepSeekMessage message;

        public DeepSeekMessage getMessage() {
            return message;
        }

        public void setMessage(DeepSeekMessage message) {
            this.message = message;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DeepSeekMessage {
        private String role;
        private String content;

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DeepSeekError {
        private String message;
        @JsonProperty("type")
        private String type;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}

