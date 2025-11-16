package com.example.train_back.controller;

import com.example.train_back.dto.InvestorProfileResp;
import com.example.train_back.dto.OtcOpenReq;
import com.example.train_back.dto.OtcStatusResponse;
import com.example.train_back.service.InvestorService;
import com.example.train_back.dto.ApiResponse;   // 用你项目里现有的响应类
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/investor")
public class InvestorController {

    @Autowired
    private InvestorService investorService;

    @GetMapping("/otc/status")
    public ApiResponse<OtcStatusResponse> getOtcStatus() {
        // TODO: 这里先写死一个用户ID，后续用登录信息替换
        Integer userId = 19;

        OtcStatusResponse data = investorService.getOtcStatus(userId);
        return ApiResponse.success(data);
    }

    @GetMapping("/profile")
    public ApiResponse<InvestorProfileResp> getProfile() {
        Integer userId = 19; // TODO: 同上，替换成真实登录用户ID
        InvestorProfileResp data = investorService.getInvestorProfile(userId);
        return ApiResponse.success(data);
    }

    @PostMapping("/otc/open")
    public ApiResponse<Void> openOtc(@RequestBody OtcOpenReq req) {
        Integer userId = 19; // TODO: 从登录态拿
        investorService.openOtcAccount(userId, req);
        return ApiResponse.success(null);
    }
}
