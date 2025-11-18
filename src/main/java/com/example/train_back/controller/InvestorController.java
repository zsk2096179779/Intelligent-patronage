package com.example.train_back.controller;

import com.example.train_back.dto.InvestorProfileResp;
import com.example.train_back.dto.OtcOpenReq;
import com.example.train_back.dto.OtcStatusResponse;
import com.example.train_back.service.InvestorService;
import com.example.train_back.dto.ApiResponse;   // 用你项目里现有的响应类
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/investor")
public class InvestorController {

    @Autowired
    private InvestorService investorService;

    @GetMapping("/otc/status")
    public ApiResponse<OtcStatusResponse> getOtcStatus(HttpServletRequest request) {
        // TODO: 这里先写死一个用户ID，后续用登录信息替换
        Integer userId = getCurrentUserIdOrNull(request);
//        Integer userId = 16;

        OtcStatusResponse data = investorService.getOtcStatus(userId);
        return ApiResponse.success(data);
    }

    @GetMapping("/profile")
    public ApiResponse<InvestorProfileResp> getProfile(HttpServletRequest request) {
        Integer userId = getCurrentUserIdOrNull(request);
//        Integer userId = 16; // TODO: 同上，替换成真实登录用户ID
        InvestorProfileResp data = investorService.getInvestorProfile(userId);
        return ApiResponse.success(data);
    }

    @PostMapping("/otc/open")
    public ApiResponse<Void> openOtc(@RequestBody OtcOpenReq req,HttpServletRequest request) {
        Integer userId = getCurrentUserIdOrNull(request);
//        Integer userId = 16; // TODO: 从登录态拿
        investorService.openOtcAccount(userId, req);
        return ApiResponse.success(null);
    }

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
}
