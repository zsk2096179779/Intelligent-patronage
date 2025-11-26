package com.example.train_back.mapper;

import com.example.train_back.entity.InvestorProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface InvestorProfileMapper {

    /**
     * 根据用户ID查询投资者画像
     */
    InvestorProfile selectByUserId(@Param("userId") Integer userId);

    int insert(InvestorProfile profile);

    int updateOtcInfoByUserId(InvestorProfile profile);

    int updateRiskInfoByUserId(com.example.train_back.entity.InvestorProfile profile);
}
