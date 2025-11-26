package com.example.train_back.mapper;

import com.example.train_back.entity.RiskQuestionnaire;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RiskQuestionnaireMapper {

    /**
     * 查询所有题目（目前先不区分版本）
     */
    List<RiskQuestionnaire> selectAll();
}
