package com.example.train_back.mapper;

import com.example.train_back.entity.SettlementOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SettlementOrderMapper {

    int insert(SettlementOrder record);

    int insertBatch(@Param("list") List<SettlementOrder> list);

    List<SettlementOrder> selectByBatchNo(@Param("batchNo") String batchNo);
}
