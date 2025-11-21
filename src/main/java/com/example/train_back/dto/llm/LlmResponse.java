package com.example.train_back.dto.llm;

public class LlmResponse {
    private String reply;
    private Object raw;

    public LlmResponse() {}

    public LlmResponse(String reply, Object raw) {
        this.reply = reply;
        this.raw = raw;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public Object getRaw() {
        return raw;
    }

    public void setRaw(Object raw) {
        this.raw = raw;
    }
}
