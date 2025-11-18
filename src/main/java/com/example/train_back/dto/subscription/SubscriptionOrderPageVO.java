package com.example.train_back.dto.subscription;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter@Setter
public class SubscriptionOrderPageVO {

    private long total;
    private int page;
    private int pageSize;
    private int totalPages;

    private List<SubscriptionOrderSummaryVO> list;
}
