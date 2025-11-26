package com.example.backend.service;

import com.example.backend.entity.RebalanceBacktestResult;
import com.example.backend.mapper.RebalanceBacktestMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class RebalanceBacktestService {

    private static final Logger log = LoggerFactory.getLogger(RebalanceBacktestService.class);

    private final RebalanceBacktestMapper mapper;

    public RebalanceBacktestService(RebalanceBacktestMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 保存回测结果到数据库（如果表存在）
     * @return 回测结果对象，如果保存失败则只返回内存中的结果
     */
    public RebalanceBacktestResult recordResult(Long strategyId,
                                                BigDecimal cumulativeReturn,
                                                BigDecimal maxDrawdown,
                                                BigDecimal sharpeRatio,
                                                Integer trades) {
        RebalanceBacktestResult result = new RebalanceBacktestResult();
        result.setStrategyId(strategyId);
        result.setCumulativeReturn(cumulativeReturn);
        result.setMaxDrawdown(maxDrawdown);
        result.setSharpeRatio(sharpeRatio);
        result.setTrades(trades);
        result.setRunAt(LocalDateTime.now());
        
        // 尝试保存到数据库，如果表不存在则跳过
        try {
            mapper.insert(result);
            log.debug("回测结果已保存到数据库: strategyId={}", strategyId);
        } catch (Exception e) {
            // 如果表不存在或其他数据库错误，只记录日志，不抛出异常
            log.warn("保存回测结果到数据库失败（表可能不存在）: {}", e.getMessage());
            // 继续返回结果对象，即使没有保存到数据库
        }
        
        return result;
    }

    /**
     * 查找最新的回测结果（如果表存在）
     */
    public RebalanceBacktestResult findLatest(Long strategyId) {
        try {
            return mapper.findLatestByStrategyId(strategyId);
        } catch (Exception e) {
            log.warn("查询回测结果失败（表可能不存在）: {}", e.getMessage());
            return null;
        }
    }
}

