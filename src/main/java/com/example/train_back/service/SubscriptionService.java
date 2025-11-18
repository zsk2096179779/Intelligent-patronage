package com.example.train_back.service;

import com.example.train_back.dto.subscription.*;

public interface SubscriptionService {

    // 4.5.1 创建签约订单（草稿）
    SubscriptionCreateResp createOrder(Integer userId, SubscriptionCreateReq req);

    // 4.5.2 获取订单详情
    SubscriptionDetailVO getOrderDetail(Integer userId, String orderNo);

    // 4.5.3 更新订单信息（仅草稿）
    SubscriptionUpdateResp updateOrder(Integer userId, String orderNo, SubscriptionUpdateReq req);

    // 4.5.4 保存电子签名
    SubscriptionSignatureResp saveSignature(Integer userId, String orderNo, SubscriptionSignatureReq req);

    // 4.5.5 提交订单（进入审核流程）
    SubscriptionSubmitResp submitOrder(Integer userId, String orderNo, SubscriptionSubmitReq req);

    // 4.5.6 确认风险不匹配
    ConfirmRiskMismatchResp confirmRiskMismatch(Integer userId, String orderNo, ConfirmRiskMismatchReq req);

    // 4.5.7 取消订单
    SubscriptionCancelResp cancelOrder(Integer userId, String orderNo, SubscriptionCancelReq req);

    // 4.5.8 查询当前用户订单列表
    SubscriptionOrderPageVO listMyOrders(Integer userId, String status, Integer page, Integer pageSize);
}
