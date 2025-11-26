package com.example.train_back.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter@Setter
public class TradeOrder {

    /** 主键 tradeid */
    private Integer tradeId;

    private Integer customerId;
    private String fundCode;
    private Integer portfolioId;
    private Integer rebalanceId;

    private Double amount;
    private Double shares;

    private String tradeType;   // 申购/赎回/调仓等
    private String reason;
    private Date tradeTime;
    private String status;
    private String failReason;
    private Integer replaceOrderId;
}
