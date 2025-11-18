package com.example.train_back.entity;

import lombok.Getter;
import lombok.Setter;


@Setter @Getter
public class SettlementOrder {
    private Integer id;
    private String batchNo;
    private String portfolioName;
    private Integer portfolioId;
    private String fundName;
    private String fundCode;
    private Double ratio;
}
