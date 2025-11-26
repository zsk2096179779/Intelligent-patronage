package com.example.train_back.dto.subscription;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter@Setter
public class SubscriptionUpdateResp {
    private String orderNo;
    private LocalDateTime updatedAt;
}
