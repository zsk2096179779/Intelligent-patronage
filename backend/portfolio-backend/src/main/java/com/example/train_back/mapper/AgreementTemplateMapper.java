package com.example.train_back.mapper;

import com.example.train_back.entity.AgreementTemplate;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AgreementTemplateMapper {

    AgreementTemplate selectById(@Param("id") Integer id);

    List<AgreementTemplate> selectByScenario(@Param("scenario") String scenario);

    int increaseViewCount(@Param("id") Integer id);

    int increaseSignCount(@Param("id") Integer id);
}
