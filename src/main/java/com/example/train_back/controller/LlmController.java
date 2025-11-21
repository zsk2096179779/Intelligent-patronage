package com.example.train_back.controller;

import com.example.train_back.dto.llm.LlmRequest;
import com.example.train_back.dto.llm.LlmResponse;
import com.example.train_back.service.llm.LlmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class LlmController {
    private static final Logger logger = LoggerFactory.getLogger(LlmController.class);

    @Autowired
    private LlmService llmService;

    @PostMapping("/llm")
    public ResponseEntity<?> proxyLlm(@RequestBody LlmRequest request, Principal principal, HttpServletRequest httpRequest) {
        String userKey = principal != null ? principal.getName() : null;
        String clientIp = httpRequest.getRemoteAddr();
        try {
            var resp = llmService.proxyRequest(request, userKey, clientIp);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException ex) {
            logger.info("LLM request validation failed: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        } catch (IllegalStateException ex) {
            String msg = ex.getMessage();
            if ("rate_limit_exceeded".equals(msg)) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of("error", "rate_limit_exceeded"));
            }
            if (msg != null && msg.startsWith("upstream_http_error:")) {
                // format: upstream_http_error:STATUS:BODY
                String[] parts = msg.split(":", 3);
                int status = 502;
                String body = "";
                try { status = Integer.parseInt(parts[1]); } catch (Exception ignored) {}
                if (parts.length >= 3) body = parts[2];
                logger.warn("Upstream returned {}: {}", status, body);
                return ResponseEntity.status(status).body(Map.of("error", "upstream_error", "details", body));
            }
            if ("provider_api_key_missing".equals(msg)) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "provider_api_key_missing"));
            }
            logger.error("LLM service state error", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "service_error"));
        } catch (Exception ex) {
            logger.error("LLM proxy failed", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "proxy_failed"));
        }
    }
}
