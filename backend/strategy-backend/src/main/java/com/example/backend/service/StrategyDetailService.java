package com.example.backend.service;

import com.example.backend.entity.*;
import com.example.backend.mapper.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StrategyDetailService {

    private final StrategyStatisticsMapper statisticsMapper;
    private final StrategyReturnChartMapper returnChartMapper;
    private final StrategyHoldingMapper holdingMapper;
    private final StrategyTradeHistoryMapper tradeHistoryMapper;

    public StrategyDetailService(StrategyStatisticsMapper statisticsMapper,
                                 StrategyReturnChartMapper returnChartMapper,
                                 StrategyHoldingMapper holdingMapper,
                                 StrategyTradeHistoryMapper tradeHistoryMapper) {
        this.statisticsMapper = statisticsMapper;
        this.returnChartMapper = returnChartMapper;
        this.holdingMapper = holdingMapper;
        this.tradeHistoryMapper = tradeHistoryMapper;
    }

    public StrategyStatistics getStatistics(Long strategyId) {
        return statisticsMapper.findByStrategyId(strategyId);
    }

    public List<StrategyReturnPoint> getReturnPoints(Long strategyId) {
        return returnChartMapper.findByStrategyId(strategyId);
    }

    public List<StrategyHolding> getHoldings(Long strategyId) {
        return holdingMapper.findByStrategyId(strategyId);
    }

    public List<StrategyTradeHistory> getTradeHistory(Long strategyId) {
        return tradeHistoryMapper.findByStrategyId(strategyId);
    }
}

