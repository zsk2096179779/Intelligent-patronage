package com.example.train_back.dto.subscription;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter@Setter
public class SubscriptionCreateResp {

    private String orderNo;
    private String orderStatus;
    private LocalDateTime createdAt;
}
