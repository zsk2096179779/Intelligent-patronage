package com.example.train_back.service.llm;

import com.example.train_back.dto.llm.LlmRequest;
import com.example.train_back.dto.llm.LlmResponse;

public interface LlmService {
    LlmResponse proxyRequest(LlmRequest request, String userKey, String clientIp) throws Exception;
}
