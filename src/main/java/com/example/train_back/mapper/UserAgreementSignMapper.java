package com.example.train_back.mapper;

import com.example.train_back.entity.UserAgreementSign;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserAgreementSignMapper {

    int insert(UserAgreementSign record);

    UserAgreementSign selectLatestByUserAndAgreement(@Param("userId") Integer userId,
                                                     @Param("agreementId") Integer agreementId,
                                                     @Param("scenario") String scenario);

    List<UserAgreementSign> selectByUserAndScenario(@Param("userId") Integer userId,
                                                    @Param("scenario") String scenario);
}
