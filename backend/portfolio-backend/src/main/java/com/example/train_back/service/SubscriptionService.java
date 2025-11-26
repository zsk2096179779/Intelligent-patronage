package com.example.train_back.service;

import com.example.train_back.dto.subscription.*;

public interface SubscriptionService {

    // 4.5.2 获取订单详情
    SubscriptionDetailVO getOrderDetail(Integer userId, String orderNo);

    // 4.5.3 更新订单信息（仅草稿）
    SubscriptionUpdateResp updateOrder(Integer userId, String orderNo, SubscriptionUpdateReq req);

    // 4.5.4 保存电子签名
    SubscriptionSignatureResp saveSignature(Integer userId, String orderNo, SubscriptionSignatureReq req);

    // 4.5.5 创建并提交订单
    SubscriptionSubmitResp submitOrder(Integer userId, SubscriptionSubmitReq req);

    // 4.5.x 判断是否已购买指定组合
    boolean hasPurchasedPortfolio(Integer userId, Integer portfolioId);

    // 4.5.6 确认风险不匹配
    ConfirmRiskMismatchResp confirmRiskMismatch(Integer userId, String orderNo, ConfirmRiskMismatchReq req);

    // 4.5.7 取消订单
    SubscriptionCancelResp cancelOrder(Integer userId, String orderNo, SubscriptionCancelReq req);

    // 4.5.8 查询当前用户订单列表
    SubscriptionOrderPageVO listMyOrders(Integer userId, String status, Integer page, Integer pageSize);
    
    // 4.5.9 查询已购买的组合产品列表（包含组合详情）
    java.util.List<PurchasedPortfolioVO> getPurchasedPortfolios(Integer userId);
}
