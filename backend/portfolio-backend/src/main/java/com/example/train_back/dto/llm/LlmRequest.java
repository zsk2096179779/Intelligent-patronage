package com.example.train_back.dto.llm;

import java.util.List;

public class LlmRequest {
    private List<LlmMessage> messages;
    private Object options;

    public LlmRequest() {}

    public LlmRequest(List<LlmMessage> messages, Object options) {
        this.messages = messages;
        this.options = options;
    }

    public List<LlmMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<LlmMessage> messages) {
        this.messages = messages;
    }

    public Object getOptions() {
        return options;
    }

    public void setOptions(Object options) {
        this.options = options;
    }
}
