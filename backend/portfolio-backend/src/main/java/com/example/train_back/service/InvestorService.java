package com.example.train_back.service;

import com.example.train_back.dto.InvestorProfileResp;
import com.example.train_back.dto.OtcOpenReq;
import com.example.train_back.dto.OtcStatusResponse;
import org.springframework.stereotype.Service;

@Service
public interface InvestorService {

    /**
     * 查询当前用户的OTC开通状态
     */
    OtcStatusResponse getOtcStatus(Integer userId);

    InvestorProfileResp getInvestorProfile(Integer userId);

    void openOtcAccount(Integer userId, OtcOpenReq req);
}
