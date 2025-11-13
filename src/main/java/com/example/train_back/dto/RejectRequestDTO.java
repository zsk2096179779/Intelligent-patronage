package com.example.train_back.dto;

/**
 * 审核拒绝请求DTO
 */
public class RejectRequestDTO {
    /**
     * 拒绝原因
     */
    private String reason;

    public RejectRequestDTO() {
    }

    public RejectRequestDTO(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "RejectRequestDTO{" +
                "reason='" + reason + '\'' +
                '}';
    }
}

