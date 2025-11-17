package com.example.train_back.controller;

import com.example.train_back.dto.*;
import com.example.train_back.service.RiskAssessmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/risk-assessment")
public class RiskAssessmentController {

    @Autowired
    private RiskAssessmentService riskAssessmentService;

    @GetMapping("/status")
    public ApiResponse<RiskStatusResp> status() {
        // TODO: userId 先写死，后面接入登录后再改
        Integer userId = 19;

        RiskStatusResp data = riskAssessmentService.getRiskStatus(userId);
        return ApiResponse.success(data);
    }

    @PostMapping("/match-check")
    public ApiResponse<RiskMatchCheckResp> matchCheck(@RequestBody RiskMatchCheckReq req) {
        Integer userId = 19; // TODO: 同上
        RiskMatchCheckResp data = riskAssessmentService.checkRiskMatch(userId, req);
        return ApiResponse.success(data);
    }

    /**
     * 4.3.1 获取问卷
     */
    @GetMapping("/questionnaire")
    public ApiResponse<RiskQuestionnaireResp> getQuestionnaire(
            @RequestParam(value = "version", required = false) String version) {

        RiskQuestionnaireResp data = riskAssessmentService.getQuestionnaire(version);
        return ApiResponse.success(data);
    }

    /**
     * 4.3.2 开始测评
     */
    @PostMapping("/start")
    public ApiResponse<RiskAssessmentStartResp> startAssessment() {
        // TODO: 从登录态获取 userId，这里先写死便于联调
        Integer userId = 19;
        RiskAssessmentStartResp data = riskAssessmentService.startAssessment(userId);
        return ApiResponse.success(data);
    }

    /**
     * 4.3.3 提交答案
     */
    @PostMapping("/submit")
    public ApiResponse<RiskAssessmentSubmitResp> submitAssessment(
            @RequestBody RiskAssessmentSubmitReq req) {

        Integer userId = 19;  // TODO: 从登录态获取真实userid
        RiskAssessmentSubmitResp data = riskAssessmentService.submitAssessment(userId, req);
        return ApiResponse.success(data);
    }
}
