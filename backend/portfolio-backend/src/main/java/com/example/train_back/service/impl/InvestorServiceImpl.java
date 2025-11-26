package com.example.train_back.service.impl;

import com.example.train_back.dto.InvestorProfileResp;
import com.example.train_back.dto.OtcOpenReq;
import com.example.train_back.dto.OtcStatusResponse;
import com.example.train_back.entity.InvestorProfile;
import com.example.train_back.mapper.InvestorProfileMapper;
import com.example.train_back.service.InvestorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
public class InvestorServiceImpl implements InvestorService {

    @Autowired
    private InvestorProfileMapper investorProfileMapper;

    @Override
    public OtcStatusResponse getOtcStatus(Integer userId) {
        InvestorProfile profile = investorProfileMapper.selectByUserId(userId);

        OtcStatusResponse resp = new OtcStatusResponse();
        if (profile == null || profile.getOtcStatus() == null
                || profile.getOtcStatus() == 0) {
            // 没有画像记录或者状态为0，都视为未开通
            resp.setOpened(false);
            resp.setAccountNo(null);
            resp.setOpenDate(null);
        } else {
            resp.setOpened(true);
            resp.setAccountNo(profile.getOtcAccountNo());
            resp.setOpenDate(profile.getOtcOpenDate());
        }
        return resp;
    }
    @Override
    public InvestorProfileResp getInvestorProfile(Integer userId) {
        InvestorProfile profile = investorProfileMapper.selectByUserId(userId);

        // 如果没有画像记录，返回一个空壳对象，避免前端 NPE
        if (profile == null) {
            InvestorProfileResp empty = new InvestorProfileResp();
            empty.setOtcStatus(0);
            empty.setRiskExpired(true);
            return empty;
        }

        InvestorProfileResp resp = new InvestorProfileResp();
        // 基本信息
        resp.setRealName(profile.getRealName());
        resp.setMobile(profile.getMobile());
        resp.setBankName(profile.getBankName());
        resp.setBankCardNo(profile.getBankCardNo());

        // OTC 信息
        resp.setOtcStatus(profile.getOtcStatus());
        resp.setOtcAccountNo(profile.getOtcAccountNo());
        resp.setOtcOpenDate(profile.getOtcOpenDate());

        // 风险测评信息
        resp.setRiskLevel(profile.getRiskLevel());
        resp.setRiskScore(profile.getRiskScore());
        resp.setRiskAssessmentDate(profile.getRiskAssessmentDate());
        resp.setRiskExpireDate(profile.getRiskExpireDate());

        // 是否过期（为空就视为已过期或未做测评）
        Boolean expired = true;
        LocalDateTime expire = profile.getRiskExpireDate();
        if (expire != null) {
            expired = expire.isBefore(LocalDateTime.now());
        }
        resp.setRiskExpired(expired);

        return resp;
    }

    @Override
    public void openOtcAccount(Integer userId, OtcOpenReq req) {
        // 1. 先查有没有已有画像
        InvestorProfile profile = investorProfileMapper.selectByUserId(userId);
        boolean isNew = false;
        if (profile == null) {
            profile = new InvestorProfile();
            profile.setUserId(userId);
            profile.setStatus(1); // 有效
            isNew = true;
        }

        // 2. 填充基本信息
        profile.setRealName(req.getRealName());
        profile.setIdCardNo(req.getIdCardNo());
        profile.setMobile(req.getMobile());
        profile.setBankCardNo(req.getBankCardNo());
        profile.setBankName(req.getBankName());

        // 3. 设置 OTC 状态
        profile.setOtcStatus(1); // 1-已开通
        profile.setOtcAccountNo(generateOtcAccountNo(userId));
        profile.setOtcOpenDate(java.time.LocalDateTime.now());

        // 4. 插入或更新
        if (isNew) {
            investorProfileMapper.insert(profile);
        } else {
            investorProfileMapper.updateOtcInfoByUserId(profile);
        }
    }

    /**
     * 简单生成一个 OTC 账号，可以根据需要调整规则
     */
    private String generateOtcAccountNo(Integer userId) {
        java.time.format.DateTimeFormatter fmt =
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String ts = java.time.LocalDateTime.now().format(fmt);
        return "OTC" + userId + ts;
    }
}

