package com.example.train_back.service.impl;

import com.example.train_back.dto.subscription.*;
import com.example.train_back.dto.PortfolioDetailDTO;
import com.example.train_back.entity.StrategyCombination;
import com.example.train_back.entity.SubscriptionOrder;
import com.example.train_back.entity.InvestorProfile;
import com.example.train_back.mapper.StrategyCombinationMapper;
import com.example.train_back.mapper.SubscriptionOrderMapper;
import com.example.train_back.mapper.InvestorProfileMapper;
import com.example.train_back.service.SubscriptionService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionOrderMapper subscriptionOrderMapper;
    private final StrategyCombinationMapper strategyCombinationMapper;
    private final InvestorProfileMapper investorProfileMapper;
    private final ObjectMapper objectMapper;

    // ========== 4.5.2 订单详情 ==========

    @Override
    public SubscriptionDetailVO getOrderDetail(Integer userId, String orderNo) {
        SubscriptionOrder order = loadOrderOrThrow(userId, orderNo);

        StrategyCombination combo =
                strategyCombinationMapper.selectByIdForDeal(order.getPortfolioId());

        SubscriptionDetailVO vo = new SubscriptionDetailVO();
        vo.setOrderNo(order.getOrderNo());
        vo.setOrderId(order.getId());
        vo.setUserId(order.getUserId());
        vo.setPortfolioId(order.getPortfolioId());
        vo.setPortfolioName(order.getPortfolioName());
        vo.setPortfolioRiskLevel(order.getProductRiskLevel());

        vo.setSubscriptionAmount(order.getSubscriptionAmount());
        vo.setFeeAmount(order.getFeeAmount());
        vo.setActualAmount(order.getActualAmount());
        vo.setDividendMode(order.getDividendMode());
        vo.setAutoInvestEnabled(order.getAutoInvestEnabled() != null && order.getAutoInvestEnabled() == 1);
        vo.setAutoInvestPeriod(order.getAutoInvestPeriod());
        vo.setAutoInvestAmount(order.getAutoInvestAmount());

        vo.setUserRiskLevel(order.getUserRiskLevel());
        vo.setProductRiskLevel(order.getProductRiskLevel());
        vo.setRiskMatched(order.getRiskMatched() != null && order.getRiskMatched() == 1);

        // ===== 这里从组合取 feeRate =====
        vo.setFeeRate(
                combo != null && combo.getFeeRate() != null
                        ? combo.getFeeRate()
                        : BigDecimal.ZERO
        );

        // 协议列表：从 JSON 解析
        if (order.getAgreementsSigned() != null) {
            try {
                List<AgreementSignedItem> list = objectMapper.readValue(
                        order.getAgreementsSigned(),
                        new TypeReference<List<AgreementSignedItem>>() {}
                );
                vo.setAgreementsSigned(list);
            } catch (Exception e) {
                log.warn("parse agreements_signed json error, orderNo={}", orderNo, e);
                vo.setAgreementsSigned(Collections.emptyList());
            }
        } else {
            vo.setAgreementsSigned(Collections.emptyList());
        }
        vo.setAllAgreementsSigned(order.getAllAgreementsSigned() != null && order.getAllAgreementsSigned() == 1);

        vo.setSignatureData(order.getSignatureData());

        vo.setOrderStatus(order.getOrderStatus());
        vo.setPaymentStatus(order.getPaymentStatus());
        vo.setAuditStatus(order.getAuditStatus());

        vo.setCreatedAt(order.getCreatedAt());
        vo.setSubmittedAt(order.getSubmittedAt());
        vo.setPaidAt(order.getPaidAt());
        vo.setCompletedAt(order.getCompletedAt());
        vo.setCancelledAt(order.getCancelledAt());

        return vo;
    }

    // ========== 4.5.3 更新订单信息（仅草稿） ==========

    @Override
    public SubscriptionUpdateResp updateOrder(Integer userId, String orderNo, SubscriptionUpdateReq req) {
        SubscriptionOrder order = loadOrderOrThrow(userId, orderNo);

        if (!"draft".equalsIgnoreCase(order.getOrderStatus())) {
            throw new RuntimeException("仅草稿状态订单可修改");
        }

        if (req.getDividendMode() != null) {
            order.setDividendMode(req.getDividendMode());
        }
        if (req.getAutoInvestEnabled() != null) {
            order.setAutoInvestEnabled(req.getAutoInvestEnabled() ? 1 : 0);
        }
        if (req.getAutoInvestPeriod() != null) {
            order.setAutoInvestPeriod(req.getAutoInvestPeriod());
        }
        if (req.getAutoInvestAmount() != null) {
            order.setAutoInvestAmount(req.getAutoInvestAmount());
        }
        if (req.getCustomerRemark() != null) {
            order.setCustomerRemark(req.getCustomerRemark());
        }

        order.setUpdatedAt(LocalDateTime.now());
        subscriptionOrderMapper.updateById(order);

        SubscriptionUpdateResp resp = new SubscriptionUpdateResp();
        resp.setOrderNo(order.getOrderNo());
        resp.setUpdatedAt(order.getUpdatedAt());
        return resp;
    }

    // ========== 4.5.4 保存签名 ==========

    @Override
    public SubscriptionSignatureResp saveSignature(Integer userId, String orderNo, SubscriptionSignatureReq req) {
        SubscriptionOrder order = loadOrderOrThrow(userId, orderNo);

        if ("cancelled".equalsIgnoreCase(order.getOrderStatus())) {
            throw new RuntimeException("订单已取消，不能签名");
        }

        order.setSignatureData(req.getSignatureData());
        // 这里暂不记录 IP，可以后续从 Controller 传入
        LocalDateTime now = LocalDateTime.now();
        order.setSignedAt(now);
        order.setUpdatedAt(now);

        subscriptionOrderMapper.updateById(order);

        SubscriptionSignatureResp resp = new SubscriptionSignatureResp();
        resp.setOrderNo(order.getOrderNo());
        // 现在没有真正上传文件，就先返回原始 DataURL
        resp.setSignatureUrl(order.getSignatureData());
        resp.setSignedAt(order.getSignedAt());
        return resp;
    }

    // ========== 4.5.5 提交订单 ==========

    @Override
    public SubscriptionSubmitResp submitOrder(Integer userId, SubscriptionSubmitReq req) {
        if (userId == null) {
            throw new RuntimeException("未登录");
        }
        if (req == null
                || req.getPortfolioId() == null
                || req.getSubscriptionAmount() == null
                || req.getPaymentMethod() == null) {
            throw new RuntimeException("提交参数不完整");
        }

        StrategyCombination combo =
                strategyCombinationMapper.selectByIdForDeal(req.getPortfolioId());
        if (combo == null) {
            throw new RuntimeException("组合产品不存在");
        }

        // 计算费用
        BigDecimal amount = req.getSubscriptionAmount();
        BigDecimal feeRate = combo.getFeeRate() == null ? BigDecimal.ZERO : combo.getFeeRate();
        BigDecimal fee = amount.multiply(feeRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal actual = amount.subtract(fee);

        InvestorProfile profile = investorProfileMapper.selectByUserId(userId);
        String userRisk = profile != null ? profile.getRiskLevel() : null;
        String productRisk = combo.getRiskLevel();
        boolean riskMatched = calcRiskMatched(userRisk, productRisk);
        if (!riskMatched && !Boolean.TRUE.equals(req.getRiskMismatchConfirmed())) {
            throw new RuntimeException("风险不匹配未确认");
        }

        LocalDateTime now = LocalDateTime.now();
        SubscriptionOrder order = new SubscriptionOrder();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setPortfolioId(combo.getId());
        order.setPortfolioName(combo.getName());
        order.setSubscriptionAmount(amount);
        order.setFeeAmount(fee);
        order.setActualAmount(actual);
        order.setDividendMode(req.getDividendMode());
        order.setAutoInvestEnabled(Boolean.TRUE.equals(req.getAutoInvestEnabled()) ? 1 : 0);
        order.setAutoInvestPeriod(req.getAutoInvestPeriod());
        order.setAutoInvestAmount(req.getAutoInvestAmount());
        order.setUserRiskLevel(userRisk);
        order.setProductRiskLevel(productRisk);
        order.setRiskMatched(riskMatched ? 1 : 0);
        order.setRiskMismatchConfirmed(Boolean.TRUE.equals(req.getRiskMismatchConfirmed()) ? 1 : 0);
        order.setAgreementsSigned(null);
        order.setAllAgreementsSigned(0);
        order.setSignatureData(null);
        order.setSignatureIp(null);
        order.setSignedAt(null);
        order.setPaymentMethod(req.getPaymentMethod());
        order.setCustomerRemark(req.getCustomerRemark());
        order.setPaymentStatus("unpaid");
        order.setPaidAt(null);
        order.setPaymentChannelNo(null);
        order.setOrderStatus("completed");
        order.setAuditStatus("approved");
        order.setAuditorId(null);
        order.setAuditRemark(null);
        order.setAuditedAt(null);
        order.setSubmittedAt(now);
        order.setCompletedAt(now);
        order.setCancelledAt(null);
        order.setSubscribeChannel("web");
        order.setCreatedAt(now);
        order.setUpdatedAt(now);

        subscriptionOrderMapper.insert(order);

        SubscriptionSubmitResp resp = new SubscriptionSubmitResp();
        resp.setOrderNo(order.getOrderNo());
        resp.setOrderStatus(order.getOrderStatus());
        resp.setAuditStatus(order.getAuditStatus());
        resp.setSubmittedAt(order.getSubmittedAt());
        resp.setNextStep("AUDIT");
        resp.setEstimatedAuditTime("2个工作日内");
        return resp;
    }

    @Override
    public boolean hasPurchasedPortfolio(Integer userId, Integer portfolioId) {
        if (userId == null) {
            throw new RuntimeException("未登录");
        }
        if (portfolioId == null) {
            throw new RuntimeException("组合ID不能为空");
        }
        int count = subscriptionOrderMapper.countActiveByUserAndPortfolio(userId, portfolioId);
        return count > 0;
    }

    // ========== 4.5.6 确认风险不匹配 ==========

    @Override
    public ConfirmRiskMismatchResp confirmRiskMismatch(Integer userId, String orderNo, ConfirmRiskMismatchReq req) {
        SubscriptionOrder order = loadOrderOrThrow(userId, orderNo);



        if (order.getRiskMatched() != null && order.getRiskMatched() == 1) {
            throw new RuntimeException("当前订单风险本身已匹配，无需确认不匹配");
        }

        if (!Boolean.TRUE.equals(req.getConfirmed())) {
            throw new RuntimeException("用户未确认风险不匹配");
        }

        LocalDateTime now = LocalDateTime.now();
        order.setRiskMismatchConfirmed(1);
        order.setUpdatedAt(now);
        subscriptionOrderMapper.updateById(order);

        ConfirmRiskMismatchResp resp = new ConfirmRiskMismatchResp();
        resp.setOrderNo(order.getOrderNo());
        resp.setRiskMismatchConfirmed(true);
        resp.setConfirmedAt(now);

        // 这里先写死一份不匹配确认书，你后面可以改成查 agreement_template
        resp.setRequiresAdditionalAgreement(true);
        resp.setAdditionalAgreementCode("SA003");
        resp.setAdditionalAgreementTitle("风险不匹配确认书");
        return resp;
    }

    // ========== 4.5.7 取消订单 ==========

    @Override
    public SubscriptionCancelResp cancelOrder(Integer userId, String orderNo, SubscriptionCancelReq req) {
        SubscriptionOrder order = loadOrderOrThrow(userId, orderNo);

        if ("cancelled".equalsIgnoreCase(order.getOrderStatus())) {
            throw new RuntimeException("订单已是取消状态");
        }
        if ("completed".equalsIgnoreCase(order.getOrderStatus())) {
            throw new RuntimeException("订单已完成，不能取消");
        }

        LocalDateTime now = LocalDateTime.now();
        order.setOrderStatus("cancelled");
        order.setCancelledAt(now);
        order.setUpdatedAt(now);

        // 可选：把取消原因写入 audit_remark 或 customer_remark
        if (req.getCancelReason() != null) {
            order.setAuditRemark("取消原因：" + req.getCancelReason());
        }

        subscriptionOrderMapper.updateById(order);

        SubscriptionCancelResp resp = new SubscriptionCancelResp();
        resp.setOrderNo(order.getOrderNo());
        resp.setOrderStatus(order.getOrderStatus());
        resp.setCancelledAt(order.getCancelledAt());
        return resp;
    }

    // ========== 4.5.8 我的订单列表 ==========

    @Override
    public SubscriptionOrderPageVO listMyOrders(Integer userId, String status, Integer page, Integer pageSize) {
        if (page == null || page < 1) page = 1;
        if (pageSize == null || pageSize <= 0) pageSize = 10;

        int offset = (page - 1) * pageSize;

        long total = subscriptionOrderMapper.countByUserAndStatus(userId, status);
        List<SubscriptionOrder> orders =
                subscriptionOrderMapper.selectPageByUserAndStatus(userId, status, offset, pageSize);

        SubscriptionOrderPageVO pageVO = new SubscriptionOrderPageVO();
        pageVO.setTotal(total);
        pageVO.setPage(page);
        pageVO.setPageSize(pageSize);
        pageVO.setTotalPages((int) Math.ceil((double) total / pageSize));

        if (orders == null || orders.isEmpty()) {
            pageVO.setList(Collections.emptyList());
            return pageVO;
        }

        List<SubscriptionOrderSummaryVO> list = orders.stream().map(o -> {
            SubscriptionOrderSummaryVO s = new SubscriptionOrderSummaryVO();
            s.setOrderNo(o.getOrderNo());
            s.setPortfolioId(o.getPortfolioId());
            s.setPortfolioName(o.getPortfolioName());
            s.setSubscriptionAmount(o.getSubscriptionAmount());
            s.setOrderStatus(o.getOrderStatus());
            s.setPaymentStatus(o.getPaymentStatus());
            s.setAuditStatus(o.getAuditStatus());
            s.setCreatedAt(o.getCreatedAt());
            s.setSubmittedAt(o.getSubmittedAt());
            s.setPaidAt(o.getPaidAt());
            s.setCompletedAt(o.getCompletedAt());
            return s;
        }).toList();

        pageVO.setList(list);
        return pageVO;
    }

    // ========== 工具方法 ==========

    private SubscriptionOrder loadOrderOrThrow(Integer userId, String orderNo) {
        SubscriptionOrder order = subscriptionOrderMapper.selectByOrderNoAndUserId(orderNo, userId);
        if (order == null) {
            throw new RuntimeException("订单不存在或不属于当前用户");
        }
        return order;
    }

    private String generateOrderNo() {
        // 简易版：SUB + 时间戳 + 随机四位
        String random = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "SUB" + System.currentTimeMillis() + random;
    }

    // 这里复用文档的 6.2 风险映射逻辑，简单写一下
    private boolean calcRiskMatched(String userRiskCode, String productRiskText) {
        int userNum = convertUserRiskCodeToNum(userRiskCode);
        int prodNum = convertProductRiskTextToNum(productRiskText);
        if (userNum <= 0 || prodNum <= 0) {
            return false;
        }
        return userNum >= prodNum;
    }

    private int convertUserRiskCodeToNum(String code) {
        if (code == null) return 0;
        return switch (code) {
            case "C1" -> 1;
            case "C2" -> 2;
            case "C3" -> 3;
            case "C4" -> 4;
            case "C5" -> 5;
            default -> 0;
        };
    }

    private int convertProductRiskTextToNum(String level) {
        if (level == null) return 0;
        // 这里按你之前的设计：低/中低/中/中高/高
        return switch (level) {
            case "低" -> 1;
            case "中低" -> 2;
            case "中" -> 3;
            case "中高" -> 4;
            case "高" -> 5;
            default -> 0;
        };
    }

    // ========== 4.5.9 查询已购买的组合产品列表 ==========

    @Override
    public List<PurchasedPortfolioVO> getPurchasedPortfolios(Integer userId) {
        if (userId == null) {
            throw new RuntimeException("未登录");
        }

        // 1. 查询用户已完成的订单
        List<SubscriptionOrder> completedOrders = subscriptionOrderMapper.selectPageByUserAndStatus(
                userId, "completed", 0, 1000  // 假设最多1000条，实际可以分页
        );

        if (completedOrders == null || completedOrders.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 根据订单中的 portfolioId 查询组合详情并合并数据（同一组合聚合订阅金额，只保留最新一条信息）
        Map<Integer, PurchasedPortfolioVO> latestByPortfolio = new HashMap<>();
        for (SubscriptionOrder order : completedOrders) {
            if (order.getPortfolioId() == null) {
                continue;
            }

            // 查询组合详情
            PortfolioDetailDTO portfolioDetail = strategyCombinationMapper.selectPortfolioDetailById(order.getPortfolioId());
            if (portfolioDetail == null) {
                continue;
            }

            PurchasedPortfolioVO existing = latestByPortfolio.computeIfAbsent(order.getPortfolioId(), key -> {
                PurchasedPortfolioVO init = new PurchasedPortfolioVO();
                init.setPortfolioId(portfolioDetail.getPortfolioId());
                return init;
            });

            BigDecimal orderAmount = order.getSubscriptionAmount() != null ? order.getSubscriptionAmount() : BigDecimal.ZERO;
            BigDecimal currentTotal = existing.getSubscriptionAmount() != null ? existing.getSubscriptionAmount() : BigDecimal.ZERO;
            existing.setSubscriptionAmount(currentTotal.add(orderAmount));

            LocalDateTime newSubscribedAt = order.getCompletedAt() != null ? order.getCompletedAt() : order.getSubmittedAt();
            LocalDateTime newCreatedAt = order.getCreatedAt();
            LocalDateTime existTime = existing.getSubscribedAt() != null ? existing.getSubscribedAt() : existing.getCreatedAt();
            boolean newIsLater = false;
            if (newSubscribedAt == null && existTime == null) {
                newIsLater = false;
            } else if (newSubscribedAt != null && existTime == null) {
                newIsLater = true;
            } else if (newSubscribedAt != null && existTime != null) {
                newIsLater = newSubscribedAt.isAfter(existTime);
            }

            if (existing.getPortfolioName() == null || newIsLater) {
                existing.setPortfolioName(portfolioDetail.getPortfolioName());
                existing.setRiskLevel(portfolioDetail.getRiskLevel());
                existing.setStrategyType(portfolioDetail.getStrategyType());
                existing.setStrategyName(portfolioDetail.getStrategyName());
                existing.setDescription(portfolioDetail.getDescription());
                existing.setFeeRate(portfolioDetail.getFeeRate());
                existing.setPortfolioCreateTime(portfolioDetail.getCreateTime());

                existing.setReturnRate(portfolioDetail.getReturnRate());
                existing.setAnnualReturn(portfolioDetail.getAnnualReturn());
                existing.setMaxDrawdown(portfolioDetail.getMaxDrawdown());
                existing.setSharpeRatio(portfolioDetail.getSharpeRatio());
                existing.setVolatility(portfolioDetail.getVolatility());
                existing.setWinRate(portfolioDetail.getWinRate());

                existing.setOrderNo(order.getOrderNo());
                existing.setSubscribedAt(newSubscribedAt);
                existing.setCreatedAt(newCreatedAt);
            }
        }

        List<PurchasedPortfolioVO> result = new ArrayList<>(latestByPortfolio.values());

        // 按订阅时间倒序排列
        result.sort((a, b) -> {
            LocalDateTime timeA = a.getSubscribedAt() != null ? a.getSubscribedAt() : a.getCreatedAt();
            LocalDateTime timeB = b.getSubscribedAt() != null ? b.getSubscribedAt() : b.getCreatedAt();
            if (timeA == null && timeB == null) return 0;
            if (timeA == null) return 1;
            if (timeB == null) return -1;
            return timeB.compareTo(timeA);
        });

        return result;
    }
}
