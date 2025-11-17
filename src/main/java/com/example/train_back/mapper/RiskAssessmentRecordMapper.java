package com.example.train_back.mapper;

import com.example.train_back.entity.RiskAssessmentRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RiskAssessmentRecordMapper {

    int insert(RiskAssessmentRecord record);

    RiskAssessmentRecord selectByAssessmentNoAndUserId(@Param("assessmentNo") String assessmentNo,
                                                       @Param("userId") Integer userId);

    int clearCurrentByUserId(@Param("userId") Integer userId);

    RiskAssessmentRecord selectCurrentByUserId(Integer userId);

}
