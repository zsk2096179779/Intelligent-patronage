package com.example.train_back.service;

import com.example.train_back.entity.SettlementOrder;

import java.util.List;

public interface SettlementOrderService {

    List<SettlementOrder> listByPortfolio(Integer portfolioId);

    List<SettlementOrder> listByBatchNo(String batchNo);

    void saveBatch(List<SettlementOrder> list);
}
