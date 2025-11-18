package com.example.train_back.controller;

import com.example.train_back.dto.ApiResponse;
import com.example.train_back.dto.subscription.*;
import com.example.train_back.entity.SubscriptionOrder;
import com.example.train_back.service.SubscriptionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/subscription")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    /**
     * 4.5.1 创建签约订单（草稿）
     * POST /api/subscription/create
     */
    @PostMapping("/create")
    public ApiResponse<SubscriptionCreateResp> createOrder(@RequestBody SubscriptionCreateReq req,
                                                           HttpServletRequest request) {
        Integer userId = getCurrentUserIdOrThrow(request);
        SubscriptionCreateResp resp = subscriptionService.createOrder(userId, req);
        return ApiResponse.success(resp);
    }

    /**
     * 4.5.2 获取订单详情
     * GET /api/subscription/detail/{orderNo}
     */
    @GetMapping("/detail/{orderNo}")
    public ApiResponse<SubscriptionDetailVO> getOrderDetail(@PathVariable("orderNo") String orderNo,
                                                            HttpServletRequest request) {
        Integer userId = getCurrentUserIdOrThrow(request);
        SubscriptionDetailVO detail = subscriptionService.getOrderDetail(userId, orderNo);
        return ApiResponse.success(detail);
    }

    /**
     * 4.5.3 更新订单信息（仅草稿状态可更新）
     * PUT /api/subscription/update/{orderNo}
     */
    @PutMapping("/update/{orderNo}")
    public ApiResponse<SubscriptionUpdateResp> updateOrder(@PathVariable("orderNo") String orderNo,
                                                           @RequestBody SubscriptionUpdateReq req,
                                                           HttpServletRequest request) {
        Integer userId = getCurrentUserIdOrThrow(request);
        SubscriptionUpdateResp resp = subscriptionService.updateOrder(userId, orderNo, req);
        return ApiResponse.success(resp);
    }

    /**
     * 4.5.4 保存电子签名
     * POST /api/subscription/signature/{orderNo}
     */
    @PostMapping("/signature/{orderNo}")
    public ApiResponse<SubscriptionSignatureResp> saveSignature(@PathVariable("orderNo") String orderNo,
                                                                @RequestBody SubscriptionSignatureReq req,
                                                                HttpServletRequest request) {
        Integer userId = getCurrentUserIdOrThrow(request);
        SubscriptionSignatureResp resp = subscriptionService.saveSignature(userId, orderNo, req);
        return ApiResponse.success(resp);
    }

    /**
     * 4.5.5 提交订单（进入审核流程）
     * POST /api/subscription/submit/{orderNo}
     */
    @PostMapping("/submit/{orderNo}")
    public ApiResponse<SubscriptionSubmitResp> submitOrder(@PathVariable("orderNo") String orderNo,
                                                           @RequestBody SubscriptionSubmitReq req,
                                                           HttpServletRequest request) {
        Integer userId = getCurrentUserIdOrThrow(request);
        SubscriptionSubmitResp resp = subscriptionService.submitOrder(userId, orderNo, req);
        log.info("[Subscription] submitOrder risk check orderNo={}, riskMismatchConfirmed={}",
                orderNo,
                req.getRiskMismatchConfirmed());

        return ApiResponse.success(resp);
    }

    /**
     * 4.5.6 确认风险不匹配
     * POST /api/subscription/confirm-risk-mismatch/{orderNo}
     */
    @PostMapping("/confirm-risk-mismatch/{orderNo}")
    public ApiResponse<ConfirmRiskMismatchResp> confirmRiskMismatch(@PathVariable("orderNo") String orderNo,
                                                                    @RequestBody ConfirmRiskMismatchReq req,
                                                                    HttpServletRequest request) {
        Integer userId = getCurrentUserIdOrThrow(request);
        ConfirmRiskMismatchResp resp = subscriptionService.confirmRiskMismatch(userId, orderNo, req);
        log.info("[Subscription] confirmRiskMismatch userId={}, orderNo={}, confirmed={}, remark={}",
                userId,
                orderNo,
                req != null ? req.getConfirmed() : null,
                req != null ? req.getConfirmReason() : null);

        return ApiResponse.success(resp);
    }

    /**
     * 4.5.7 取消订单
     * POST /api/subscription/cancel/{orderNo}
     */
    @PostMapping("/cancel/{orderNo}")
    public ApiResponse<SubscriptionCancelResp> cancelOrder(@PathVariable("orderNo") String orderNo,
                                                           @RequestBody SubscriptionCancelReq req,
                                                           HttpServletRequest request) {
        Integer userId = getCurrentUserIdOrThrow(request);
        SubscriptionCancelResp resp = subscriptionService.cancelOrder(userId, orderNo, req);
        return ApiResponse.success(resp);
    }

    /**
     * 4.5.8 查询当前用户的订单列表
     * GET /api/subscription/my-orders
     */
    @GetMapping("/my-orders")
    public ApiResponse<SubscriptionOrderPageVO> listMyOrders(
            @RequestParam(value = "status", required = false, defaultValue = "all") String status,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {

        Integer userId = getCurrentUserIdOrThrow(request);
        SubscriptionOrderPageVO pageVO = subscriptionService.listMyOrders(userId, status, page, pageSize);
        return ApiResponse.success(pageVO);
    }

    // ========= 从 Session 读取用户ID 的工具方法（和 AgreementController 同一套路） =========

    private Integer getCurrentUserIdOrThrow(HttpServletRequest request) {
        Object uid = request.getSession().getAttribute("userId");
        if (uid instanceof Integer) {
            return (Integer) uid;
        }
        if (uid instanceof Long) {
            return ((Long) uid).intValue();
        }
        throw new RuntimeException("未登录，请先登录");
//        return 16;
    }
}
