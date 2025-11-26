package com.example.train_back.controller;

import com.example.train_back.dto.agreement.*;
import com.example.train_back.service.AgreementService;
import com.example.train_back.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/agreements")
public class AgreementController {

    private final AgreementService agreementService;

    public AgreementController(AgreementService agreementService) {
        this.agreementService = agreementService;
    }

    /**
     * 4.4.1 获取协议列表
     * GET /api/agreement/list?scenario=subscription
     * 鉴权：不需要（但如已登录可以返回 signed 状态）
     */
    @GetMapping("/list")
    public ApiResponse<List<AgreementListItemVO>> getAgreementList(@RequestParam("scenario") String scenario,
                                                                   HttpServletRequest request) {
        Integer userId = getCurrentUserIdOrNull(request);
//        Integer userId = 19;
        List<AgreementListItemVO> list = agreementService.getAgreementList(scenario, userId);
        return ApiResponse.success(list);
    }

    /**
     * 4.4.2 获取协议详情
     * GET /api/agreement/detail/{id}
     */
    @GetMapping("/detail/{id}")
    public ApiResponse<AgreementDetailVO> getAgreementDetail(@PathVariable("id") Integer id) {
        AgreementDetailVO detail = agreementService.getAgreementDetail(id);
        return ApiResponse.success(detail);
    }

    /**
     * 4.4.3 签署单个协议
     * POST /api/agreement/sign
     * 鉴权：需要
     */
    @PostMapping("/sign")
    public ApiResponse<AgreementSignResultVO> signAgreement(@RequestBody AgreementSignRequest requestBody,
                                                            HttpServletRequest request) {
        Integer userId = getCurrentUserIdOrThrow(request);
//        Integer userId = 19;
        String ip = getClientIp(request);
        String device = getDeviceInfo(request);

        AgreementSignResultVO result = agreementService.signAgreement(requestBody, userId, ip, device);
        return ApiResponse.success(result);
    }

    /**
     * 4.4.4 批量签署协议
     * POST /api/agreement/sign-batch
     */

    @PostMapping("/sign-batch")
    public ApiResponse<AgreementSignBatchResultVO> signAgreementBatch(@RequestBody AgreementSignBatchRequest requestBody,
                                                                      HttpServletRequest request) {

//        log.info("[Agreement] sign-batch 入参: {}", requestBody);

        Integer userId = getCurrentUserIdOrThrow(request);
//        Integer userId = 19;
        String ip = getClientIp(request);
        String device = getDeviceInfo(request);

        AgreementSignBatchResultVO result = agreementService
                .signAgreementBatch(requestBody, userId, ip, device);
        return ApiResponse.success(result);
    }

    // ===== 从 Session 中取当前用户ID 的简单工具方法 =====
    private Integer getCurrentUserIdOrNull(HttpServletRequest request) {
        Object uid = request.getSession().getAttribute("userId");
        if (uid instanceof Integer) {
            return (Integer) uid;
        }
        if (uid instanceof Long) {
            return ((Long) uid).intValue();
        }
        return null;
    }

    private Integer getCurrentUserIdOrThrow(HttpServletRequest request) {
        Integer userId = getCurrentUserIdOrNull(request);
        if (userId == null) {
            // 这里建议抛你统一的未登录异常，当前先简单写
            throw new RuntimeException("未登录，请先登录");
        }
        return userId;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private String getDeviceInfo(HttpServletRequest request) {
        String ua = request.getHeader("User-Agent");
        return ua == null ? "" : ua;
    }
}
