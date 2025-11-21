package com.example.train_back.service.llm.impl;

import com.example.train_back.dto.llm.LlmMessage;
import com.example.train_back.dto.llm.LlmRequest;
import com.example.train_back.dto.llm.LlmResponse;
import com.example.train_back.service.llm.LlmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DefaultLlmService implements LlmService {
    private static final Logger logger = LoggerFactory.getLogger(DefaultLlmService.class);

    @Value("${llm.provider.url:}")
    private String providerUrl;

    @Value("${llm.provider.apikey:}")
    private String providerApiKey;

    @Value("${llm.provider.type:proxy}")
    private String providerType; // openai | qianwen | deepseek | proxy

    @Value("${llm.default.model:}")
    private String defaultModel;

    // simple per-user rate limit: requests per window
    @Value("${llm.rate.limit:20}")
    private int requestsPerWindow;

    @Value("${llm.rate.window.seconds:60}")
    private int windowSeconds;

    private final RestTemplate restTemplate = new RestTemplate();

    private final Map<String, WindowCounter> counters = new ConcurrentHashMap<>();

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        if (!StringUtils.hasText(providerUrl)) {
            logger.warn("LLM provider URL not configured (llm.provider.url). The /api/llm endpoint will return mock responses unless provider is set.");
        }
        if (redisTemplate == null) {
            logger.info("Redis not configured - using in-memory rate limiter fallback.");
        } else {
            logger.info("Redis detected - using Redis for rate limiting.");
        }
    }

    @Override
    public LlmResponse proxyRequest(LlmRequest request, String userKey, String clientIp) throws Exception {
        String key = userKey != null ? userKey : clientIp;
        if (!allowRequest(key)) {
            throw new IllegalStateException("rate_limit_exceeded");
        }

        validateRequest(request);

        if (!StringUtils.hasText(providerUrl)) {
            // return a mock response when provider not configured (useful for local dev)
            StringBuilder sb = new StringBuilder();
            List<LlmMessage> msgs = request.getMessages();
            if (msgs != null) {
                for (LlmMessage m : msgs) {
                    sb.append("[").append(m.getRole()).append("] ").append(m.getContent()).append("\n");
                }
            }
            String reply = "[MOCK REPLY] Received " + (msgs == null ? 0 : msgs.size()) + " messages.\n" + sb.toString();
            return new LlmResponse(reply, null);
        }

        // Build provider-specific payload
        Object payload = buildProviderPayload(request);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // if provider requires an API key (e.g. openai) ensure it's configured
        if ("openai".equalsIgnoreCase(providerType) && !StringUtils.hasText(providerApiKey)) {
            throw new IllegalStateException("provider_api_key_missing");
        }
        if (StringUtils.hasText(providerApiKey)) {
            headers.set("Authorization", "Bearer " + providerApiKey);
        }

        HttpEntity<Object> entity = new HttpEntity<>(payload, headers);
        try {
            ResponseEntity<Object> resp = restTemplate.exchange(providerUrl, HttpMethod.POST, entity, Object.class);
            Object raw = resp.getBody();
            String reply = extractReplyFromRaw(raw);
            return new LlmResponse(reply, raw);
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            // propagate upstream error details for controller to handle
            String body = e.getResponseBodyAsString();
            String msg = "upstream_http_error:" + e.getStatusCode().value() + ":" + (body == null ? "" : body);
            throw new IllegalStateException(msg, e);
        }
    }

    @SuppressWarnings({"unchecked","rawtypes"})
    private Object buildProviderPayload(LlmRequest request) {
        // Determine model: from options.model -> request.options if map, else defaultModel
        String model = null;
        if (request.getOptions() instanceof java.util.Map) {
            java.util.Map opts = (java.util.Map) request.getOptions();
            Object m = opts.get("model");
            if (m != null) model = String.valueOf(m);
        }
        if (!StringUtils.hasText(model)) model = defaultModel;

        if ("openai".equalsIgnoreCase(providerType)) {
            java.util.Map<String, Object> body = new java.util.HashMap<>();
            if (StringUtils.hasText(model)) body.put("model", model);
            // convert messages
            java.util.List<java.util.Map<String, String>> msgs = new java.util.ArrayList<>();
            if (request.getMessages() != null) {
                for (LlmMessage m : request.getMessages()) {
                    java.util.Map<String, String> mm = new java.util.HashMap<>();
                    mm.put("role", m.getRole());
                    mm.put("content", m.getContent());
                    msgs.add(mm);
                }
            }
            body.put("messages", msgs);
            // merge other options (top-level override)
            if (request.getOptions() instanceof java.util.Map) body.putAll((java.util.Map) request.getOptions());
            return body;
        }

        if ("qianwen".equalsIgnoreCase(providerType) || "deepseek".equalsIgnoreCase(providerType)) {
            // many Chinese domestic providers accept a single prompt/input field
            StringBuilder prompt = new StringBuilder();
            if (request.getMessages() != null) {
                for (LlmMessage m : request.getMessages()) {
                    prompt.append("[").append(m.getRole()).append("] ").append(m.getContent()).append("\n");
                }
            }
            java.util.Map<String, Object> body = new java.util.HashMap<>();
            body.put("model", model != null ? model : "");
            body.put("input", prompt.toString());
            if (request.getOptions() instanceof java.util.Map) body.putAll((java.util.Map) request.getOptions());
            return body;
        }

        // default: proxy - forward original request JSON
        return request;
    }

    private boolean allowRequest(String key) {
        if (redisTemplate != null) {
            try {
                String redisKey = "llm:rate:" + key;
                Long count = redisTemplate.opsForValue().increment(redisKey);
                if (count != null && count == 1L) {
                    redisTemplate.expire(redisKey, java.time.Duration.ofSeconds(windowSeconds));
                }
                return count != null && count <= requestsPerWindow;
            } catch (Exception ex) {
                logger.warn("Redis rate limiter failed, falling back to in-memory", ex);
                // fall through to in-memory
            }
        }
        WindowCounter c = counters.computeIfAbsent(key, k -> new WindowCounter(windowSeconds));
        return c.tryAcquire(requestsPerWindow);
    }

    private void validateRequest(LlmRequest request) {
        if (request == null || request.getMessages() == null || request.getMessages().isEmpty()) {
            throw new IllegalArgumentException("messages_required");
        }

        int totalLen = 0;
        for (LlmMessage m : request.getMessages()) {
            if (m == null || m.getContent() == null) continue;
            String content = m.getContent();
            totalLen += content.length();
            if (containsForbidden(content)) {
                throw new IllegalArgumentException("input_contains_forbidden_content");
            }
        }
        if (totalLen > 4000) {
            throw new IllegalArgumentException("input_too_long");
        }
    }

    private boolean containsForbidden(String content) {
        String lower = content.toLowerCase();
        String[] banned = new String[]{"<script>", "eval(", "rm -rf", "import os", "sys.exit", "\u202e"};
        for (String b : banned) {
            if (lower.contains(b)) return true;
        }
        return false;
    }

    private String extractReplyFromRaw(Object raw) {
        if (raw == null) return "";
        // Best-effort extraction: if provider returns {choices:[{message:{content:...}}]} or {reply:...}
        try {
            if (raw instanceof java.util.Map) {
                java.util.Map<String, Object> map = (java.util.Map<String, Object>) raw;
                if (map.containsKey("reply")) return String.valueOf(map.get("reply"));
                if (map.containsKey("choices")) {
                    Object choices = map.get("choices");
                    if (choices instanceof java.util.List) {
                        java.util.List<?> list = (java.util.List<?>) choices;
                        if (!list.isEmpty() && list.get(0) instanceof java.util.Map) {
                            Object first = ((java.util.Map<?, ?>) list.get(0)).get("message");
                            if (first instanceof java.util.Map) {
                                Object content = ((java.util.Map<?, ?>) first).get("content");
                                if (content != null) return String.valueOf(content);
                            }
                            Object text = ((java.util.Map<?, ?>) list.get(0)).get("text");
                            if (text != null) return String.valueOf(text);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.debug("Failed to extract reply from raw provider response", ex);
        }
        return raw.toString();
    }

    private static class WindowCounter {
        private final int windowSeconds;
        private int count;
        private long windowStartEpochSec;

        WindowCounter(int windowSeconds) {
            this.windowSeconds = windowSeconds;
            this.windowStartEpochSec = Instant.now().getEpochSecond();
            this.count = 0;
        }

        synchronized boolean tryAcquire(int limit) {
            long now = Instant.now().getEpochSecond();
            if (now - windowStartEpochSec >= windowSeconds) {
                windowStartEpochSec = now;
                count = 0;
            }
            if (count >= limit) return false;
            count++;
            return true;
        }
    }
}
