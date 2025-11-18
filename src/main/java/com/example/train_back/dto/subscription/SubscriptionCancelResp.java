package com.example.train_back.dto.subscription;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class SubscriptionCancelResp {

    private String orderNo;
    private String orderStatus; // cancelled
    private LocalDateTime cancelledAt;
}
