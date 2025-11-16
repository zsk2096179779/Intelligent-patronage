package com.example.train_back.service.impl;

import com.example.train_back.dto.RiskAssessmentStartResp;
import com.example.train_back.dto.RiskAssessmentSubmitReq;
import com.example.train_back.dto.RiskAssessmentSubmitResp;
import com.example.train_back.dto.RiskMatchCheckReq;
import com.example.train_back.dto.RiskMatchCheckResp;
import com.example.train_back.dto.RiskQuestionnaireResp;
import com.example.train_back.dto.RiskStatusResp;
import com.example.train_back.entity.InvestorProfile;
import com.example.train_back.entity.Portfolio;
import com.example.train_back.entity.RiskAssessmentRecord;
import com.example.train_back.entity.RiskQuestionnaire;
import com.example.train_back.mapper.InvestorProfileMapper;
import com.example.train_back.mapper.PortfolioMapper;
import com.example.train_back.mapper.RiskAssessmentRecordMapper;
import com.example.train_back.mapper.RiskQuestionnaireMapper;
import com.example.train_back.service.RiskAssessmentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class RiskAssessmentServiceImpl implements RiskAssessmentService {

    private final RiskQuestionnaireMapper riskQuestionnaireMapper;
    private final RiskAssessmentRecordMapper riskAssessmentRecordMapper;
    private final InvestorProfileMapper investorProfileMapper;
    private final PortfolioMapper portfolioMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public RiskAssessmentServiceImpl(RiskQuestionnaireMapper riskQuestionnaireMapper,
                                     RiskAssessmentRecordMapper riskAssessmentRecordMapper,
                                     InvestorProfileMapper investorProfileMapper,
                                     PortfolioMapper portfolioMapper) {
        this.riskQuestionnaireMapper = riskQuestionnaireMapper;
        this.riskAssessmentRecordMapper = riskAssessmentRecordMapper;
        this.investorProfileMapper = investorProfileMapper;
        this.portfolioMapper = portfolioMapper;
    }

    // ========================= 1. 问卷相关 =========================

    @Override
    public RiskQuestionnaireResp getQuestionnaire(String version) {
        List<RiskQuestionnaire> list = riskQuestionnaireMapper.selectAll();

        RiskQuestionnaireResp resp = new RiskQuestionnaireResp();
        resp.setVersion(version != null && !version.isEmpty() ? version : "1.0");
        resp.setTotalQuestions(list.size());

        List<RiskQuestionnaireResp.QuestionItem> questions = new ArrayList<>();
        int order = 1;

        for (RiskQuestionnaire q : list) {
            RiskQuestionnaireResp.QuestionItem item = new RiskQuestionnaireResp.QuestionItem();
            item.setId(q.getId());
            item.setQuestionCode(q.getQuestionCode());
            item.setQuestionText(q.getQuestionText());
            item.setQuestionType(q.getQuestionType());
            item.setCategory(q.getCategory());
            item.setOrderNum(order++);
            item.setRequired(true);

            // 题目选项存储为 JSON 字符串（数组），这里直接解析成 List<OptionItem>
            try {
                List<RiskQuestionnaireResp.QuestionItem.OptionItem> opts =
                        objectMapper.readValue(
                                q.getOptions(),
                                objectMapper.getTypeFactory().constructCollectionType(
                                        List.class,
                                        RiskQuestionnaireResp.QuestionItem.OptionItem.class
                                )
                        );
                item.setOptions(opts);
            } catch (Exception e) {
                // 解析失败不抛异常，给一个空列表，避免整个接口挂掉
                item.setOptions(new ArrayList<>());
            }

            questions.add(item);
        }

        resp.setQuestions(questions);
        return resp;
    }

    @Override
    public RiskAssessmentStartResp startAssessment(Integer userId) {
        // 版本号先写死为 1.0，后续可以从问卷表动态获取
        String questionnaireVersion = "1.0";

        RiskAssessmentRecord record = new RiskAssessmentRecord();
        record.setUserId(userId);
        record.setAssessmentNo(generateAssessmentNo(userId));
        record.setQuestionnaireVersion(questionnaireVersion);
        record.setAnswers(null);
        record.setTotalScore(0);
        record.setRiskLevel(null);
        record.setIpAddress(null);     // 如需记录，可以从 request 里取
        record.setDeviceInfo(null);    // 如需记录，可以从 UA 里取
        record.setStartedAt(LocalDateTime.now());
        record.setCompletedAt(null);
        record.setExpireAt(null);
        record.setIsCompleted(0);
        record.setIsCurrent(0);

        riskAssessmentRecordMapper.insert(record);

        RiskAssessmentStartResp resp = new RiskAssessmentStartResp();
        resp.setAssessmentNo(record.getAssessmentNo());
        resp.setQuestionnaireVersion(questionnaireVersion);
        resp.setStartedAt(record.getStartedAt());
        return resp;
    }

    @Override
    public RiskAssessmentSubmitResp submitAssessment(Integer userId, RiskAssessmentSubmitReq req) {
        // 1) 校验测评记录是否存在
        RiskAssessmentRecord startRecord = riskAssessmentRecordMapper
                .selectByAssessmentNoAndUserId(req.getAssessmentNo(), userId);
        if (startRecord == null) {
            throw new RuntimeException("测评记录不存在，请先开始测评");
        }

        // 2) 计算总分
        int totalScore = 0;
        if (req.getAnswers() != null) {
            for (RiskAssessmentSubmitReq.AnswerItem a : req.getAnswers()) {
                if (a.getScore() != null) {
                    totalScore += a.getScore();
                }
            }
        }

        // 3) 得到风险等级映射（C1~C5 + 文案）
        RiskLevelResult risk = calcRiskLevel(totalScore);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireAt = now.plusYears(1);

        // 4) 按“结果记录”的形式再插一条，当做当前测评结果
        RiskAssessmentRecord record = new RiskAssessmentRecord();
        record.setUserId(userId);
        record.setAssessmentNo(startRecord.getAssessmentNo());
        record.setQuestionnaireVersion(startRecord.getQuestionnaireVersion());
        record.setTotalScore(totalScore);
        record.setRiskLevel(risk.code);
        record.setIpAddress(startRecord.getIpAddress());
        record.setDeviceInfo(startRecord.getDeviceInfo());
        record.setStartedAt(startRecord.getStartedAt());
        record.setCompletedAt(now);
        record.setExpireAt(expireAt);
        record.setIsCompleted(1);
        record.setIsCurrent(1);

        try {
            String answersJson = objectMapper.writeValueAsString(req.getAnswers());
            record.setAnswers(answersJson);
        } catch (JsonProcessingException e) {
            record.setAnswers(null);
        }

        // 5) 清掉当前用户之前的“当前记录”，再插入一条新的 current 记录
        riskAssessmentRecordMapper.clearCurrentByUserId(userId);
        riskAssessmentRecordMapper.insert(record);

        // 6) 回写投资者画像 investor_profile 表
        InvestorProfile profile = investorProfileMapper.selectByUserId(userId);
        if (profile == null) {
            profile = new InvestorProfile();
            profile.setUserId(userId);
        }
        profile.setRiskLevel(risk.code);
        profile.setRiskScore(totalScore);
        profile.setRiskAssessmentDate(now);
        profile.setRiskExpireDate(expireAt);

        if (profile.getId() == null) {
            investorProfileMapper.insert(profile);
        } else {
            investorProfileMapper.updateRiskInfoByUserId(profile);
        }

        // 7) 构造返回
        RiskAssessmentSubmitResp resp = new RiskAssessmentSubmitResp();
        resp.setAssessmentNo(record.getAssessmentNo());
        resp.setTotalScore(totalScore);
        resp.setRiskLevel(risk.code);
        resp.setRiskLabel(risk.label);
        resp.setDescription(risk.description);
        resp.setCompletedAt(now);
        resp.setExpireAt(expireAt);

        return resp;
    }

    // ========================= 2. 状态查询 =========================

    @Override
    public RiskStatusResp getRiskStatus(Integer userId) {
        RiskAssessmentRecord record = riskAssessmentRecordMapper.selectCurrentByUserId(userId);

        RiskStatusResp resp = new RiskStatusResp();

        if (record == null) {
            resp.setHasAssessment(false);
            resp.setRiskLevel(null);
            resp.setAssessmentDate(null);
            resp.setExpireDate(null);
            resp.setExpired(true);
            resp.setDaysUntilExpire(0);
            return resp;
        }

        resp.setHasAssessment(true);
        resp.setRiskLevel(record.getRiskLevel());
        resp.setAssessmentDate(record.getCompletedAt());
        resp.setExpireDate(record.getExpireAt());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireAt = record.getExpireAt();

        boolean expired = true;
        int days = 0;

        if (expireAt != null) {
            expired = expireAt.isBefore(now);
            long diff = Duration.between(now, expireAt).toDays();
            days = (int) diff;
            if (days < 0) {
                days = 0;
            }
        }

        resp.setExpired(expired);
        resp.setDaysUntilExpire(days);

        return resp;
    }

    // ========================= 3. 风险匹配检查 =========================

    @Override
    public RiskMatchCheckResp checkRiskMatch(Integer userId, RiskMatchCheckReq req) {
        RiskMatchCheckResp resp = new RiskMatchCheckResp();

        // 1) 用户风险等级（来自 investor_profile.risk_level）
        InvestorProfile profile = investorProfileMapper.selectByUserId(userId);
        String userRiskCode = profile != null ? profile.getRiskLevel() : null;

        // 2) 产品风险等级（来自 portfolios.risk_level）
        Portfolio portfolio = null;
        if (req != null && req.getPortfolioId() != null) {
            portfolio = portfolioMapper.selectById(req.getPortfolioId());
        }
        String productRiskRaw = portfolio != null ? portfolio.getRiskLevel() : null;

        // 3) 数值化比较
        int userRiskNum = convertUserRiskCodeToNum(userRiskCode);
        String productRiskText = normalizeProductRiskLevel(productRiskRaw);
        int productRiskNum = convertProductRiskTextToNum(productRiskText);

        resp.setUserRiskLevel(userRiskCode);
        resp.setProductRiskLevel(productRiskText);
        resp.setProductRiskLevelNum(productRiskNum);

        // 任一风险等级无效：直接不匹配且不可购买
        if (userRiskNum <= 0 || productRiskNum <= 0) {
            resp.setMatched(false);
            resp.setCanPurchase(false);
            resp.setNeedConfirm(false);
            resp.setWarningMessage("当前无法识别有效的风险等级，请先完成风险测评或联系工作人员。");
            return resp;
        }

        // 规则：用户风险数值 >= 产品风险数值 → 匹配
        if (userRiskNum >= productRiskNum) {
            resp.setMatched(true);
            resp.setCanPurchase(true);
            resp.setNeedConfirm(false);
            resp.setWarningMessage(null);
        } else {
            resp.setMatched(false);
            resp.setCanPurchase(false);
            resp.setNeedConfirm(true);

            String msg = String.format(
                    "该产品风险等级为【%s】，高于您的风险承受能力（%s级）。" +
                            "根据监管要求，不建议购买此产品。如坚持购买，需签署《风险不匹配确认书》。",
                    productRiskText, userRiskCode
            );
            resp.setWarningMessage(msg);
        }

        return resp;
    }

    // ========================= 4. 内部工具方法 =========================

    /**
     * 生成测评流水号：ASSESS + userId + yyyyMMddHHmmss
     */
    private String generateAssessmentNo(Integer userId) {
        java.time.format.DateTimeFormatter fmt =
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String ts = LocalDateTime.now().format(fmt);
        return "ASSESS" + userId + ts;
    }

    /**
     * 得分 -> 风险等级映射：
     *   0-20: C1 保守型
     *  21-30: C2 稳健型
     *  31-40: C3 平衡型
     *  41-45: C4 积极型
     *  46-  : C5 激进型
     */
    private RiskLevelResult calcRiskLevel(int totalScore) {
        RiskLevelResult r = new RiskLevelResult();
        if (totalScore <= 20) {
            r.code = "C1";
            r.label = "保守型";
            r.description = "您整体风险承受能力较低，适合以低风险产品为主。";
        } else if (totalScore <= 30) {
            r.code = "C2";
            r.label = "稳健型";
            r.description = "您风险承受能力偏稳健，适合配置低至中低风险产品。";
        } else if (totalScore <= 40) {
            r.code = "C3";
            r.label = "平衡型";
            r.description = "您具有平衡的风险偏好，适合投资中等风险的产品。";
        } else if (totalScore <= 45) {
            r.code = "C4";
            r.label = "积极型";
            r.description = "您具有较高的风险承受能力，可配置中高风险产品。";
        } else {
            r.code = "C5";
            r.label = "激进型";
            r.description = "您具有较强的风险偏好，适合配置高风险产品。";
        }
        return r;
    }

    /**
     * 用户风险等级 C1~C5 -> 数值 1~5
     */
    private int convertUserRiskCodeToNum(String code) {
        if (code == null) {
            return 0;
        }
        switch (code.trim().toUpperCase()) {
            case "C1":
                return 1;
            case "C2":
                return 2;
            case "C3":
                return 3;
            case "C4":
                return 4;
            case "C5":
                return 5;
            default:
                return 0;
        }
    }

    /**
     * 归一化产品风险等级文案：
     * 例如 "中低风险" / "中低风险型" / "中低风险产品" -> "中低"
     */
    private String normalizeProductRiskLevel(String raw) {
        if (raw == null) {
            return null;
        }
        String s = raw.replace("风险型", "")
                .replace("风险", "")
                .replace("型", "")
                .replace("产品", "")
                .replace(" ", "")
                .trim();
        return s;
    }

    /**
     * 产品风险等级（低/中低/中/中高/高） -> 数值 1~5
     */
    private int convertProductRiskTextToNum(String text) {
        if (text == null) {
            return 0;
        }
        switch (text) {
            case "低":
                return 1;
            case "中低":
                return 2;
            case "中":
                return 3;
            case "中高":
                return 4;
            case "高":
                return 5;
            default:
                return 0;
        }
    }

    private static class RiskLevelResult {
        String code;
        String label;
        String description;
    }
}
