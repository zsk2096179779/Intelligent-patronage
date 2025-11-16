package com.example.train_back.service;

import com.example.train_back.dto.RiskQuestionnaireResp;
import com.example.train_back.dto.RiskAssessmentStartResp;
import com.example.train_back.dto.RiskAssessmentSubmitReq;
import com.example.train_back.dto.RiskAssessmentSubmitResp;
import com.example.train_back.dto.RiskStatusResp;
import com.example.train_back.dto.RiskMatchCheckReq;
import com.example.train_back.dto.RiskMatchCheckResp;

public interface RiskAssessmentService {

    RiskQuestionnaireResp getQuestionnaire(String version);

    RiskAssessmentStartResp startAssessment(Integer userId);

    RiskAssessmentSubmitResp submitAssessment(Integer userId, RiskAssessmentSubmitReq req);

    // 你之前已经有的：
    RiskStatusResp getRiskStatus(Integer userId);

    RiskMatchCheckResp checkRiskMatch(Integer userId, RiskMatchCheckReq req);
}
