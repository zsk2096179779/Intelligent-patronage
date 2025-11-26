package com.example.train_back.mapper;

import com.example.train_back.entity.SubscriptionOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SubscriptionOrderMapper {

    int insert(SubscriptionOrder order);

    SubscriptionOrder selectByOrderNoAndUserId(@Param("orderNo") String orderNo,
                                               @Param("userId") Integer userId);

    int updateById(SubscriptionOrder order);

    long countByUserAndStatus(@Param("userId") Integer userId,
                              @Param("status") String status);

    List<SubscriptionOrder> selectPageByUserAndStatus(@Param("userId") Integer userId,
                                                      @Param("status") String status,
                                                      @Param("offset") int offset,
                                                      @Param("limit") int limit);

    int countActiveByUserAndPortfolio(@Param("userId") Integer userId,
                                      @Param("portfolioId") Integer portfolioId);
}
