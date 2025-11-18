package com.example.train_back.service;

import com.example.train_back.entity.SubscriptionOrder;

import java.math.BigDecimal;
import java.util.List;

public interface SubscriptionOrderService {

    /**
     * 创建签约订单（Controller 里把 DTO 转成 SubscriptionOrder 再调）
     */
    SubscriptionOrder createOrder(SubscriptionOrder order);

    /**
     * 标记订单支付成功（结合你的支付/模拟支付逻辑）
     */
    void markPaid(String orderNo,
                  BigDecimal paidAmount,
                  String paymentMethod,
                  String paymentChannelNo);

    /**
     * 取消订单
     */
    void cancelOrder(String orderNo, Integer userId);

    /**
     * 查询当前用户的订单
     */
    List<SubscriptionOrder> listMyOrders(Integer userId, String orderStatus);

    /**
     * 获取订单详情（带权限校验）
     */
    SubscriptionOrder getOrderDetail(String orderNo, Integer userId);
}
