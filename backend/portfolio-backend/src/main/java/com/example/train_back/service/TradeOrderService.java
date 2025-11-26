package com.example.train_back.service;

import com.example.train_back.entity.TradeOrder;

import java.util.List;

public interface TradeOrderService {

    int createTradeOrder(TradeOrder tradeOrder);

    int createTradeOrdersBatch(List<TradeOrder> trades);

    List<TradeOrder> listByCustomer(Integer customerId);

    List<TradeOrder> listByPortfolio(Integer portfolioId);
}
