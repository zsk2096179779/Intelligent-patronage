package com.example.train_back.mapper;

import com.example.train_back.entity.TradeOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TradeOrderMapper {

    int insert(TradeOrder record);

    int insertBatch(@Param("list") List<TradeOrder> list);

    TradeOrder selectById(@Param("id") Integer id);
}
