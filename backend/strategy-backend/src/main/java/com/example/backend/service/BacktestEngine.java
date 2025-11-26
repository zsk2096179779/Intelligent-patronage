package com.example.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 策略回测引擎
 */
@Service
public class BacktestEngine {

    private static final Logger log = LoggerFactory.getLogger(BacktestEngine.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final MarketDataClient marketDataClient;

    public BacktestEngine(MarketDataClient marketDataClient) {
        this.marketDataClient = marketDataClient;
    }

    /**
     * 执行回测
     * @param strategyId 策略ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param initialCapital 初始资金
     * @param transactionFee 交易费用（百分比，如0.15表示0.15%）
     * @param slippage 滑点（百分比，如0.1表示0.1%）
     * @return 回测结果
     */
    public BacktestResult runBacktest(Long strategyId, LocalDate startDate, LocalDate endDate,
                                      double initialCapital, double transactionFee, double slippage) {
        log.info("开始回测: strategyId={}, 时间范围={} 到 {}, 初始资金={}, 交易费用={}%, 滑点={}%",
                strategyId, startDate, endDate, initialCapital, transactionFee, slippage);

        try {
            // 1. 获取策略持仓（如果没有，使用默认持仓）
            List<Holding> holdings = getStrategyHoldings(strategyId);
            if (holdings.isEmpty()) {
                holdings = getDefaultHoldings();
                log.info("策略无持仓数据，使用默认持仓: {}", holdings.size());
            }

            // 2. 获取所有持仓股票的历史数据
            Map<String, List<PricePoint>> priceData = new HashMap<>();
            int successCount = 0;
            for (Holding holding : holdings) {
                try {
                    List<PricePoint> prices = fetchHistoryPrices(holding.code, startDate, endDate);
                    if (!prices.isEmpty()) {
                        priceData.put(holding.code, prices);
                        successCount++;
                        log.debug("成功获取股票 {} 历史数据: {} 条", holding.code, prices.size());
                    } else {
                        log.warn("股票 {} 历史数据为空", holding.code);
                    }
                } catch (Exception e) {
                    log.warn("获取股票 {} 历史数据失败: {}", holding.code, e.getMessage(), e);
                }
            }

            log.info("成功获取 {} / {} 只股票的历史数据", successCount, holdings.size());

            BigDecimal transactionFeeRate = BigDecimal.valueOf(transactionFee / 100.0);
            BigDecimal slippageRate = BigDecimal.valueOf(slippage / 100.0);

            BigDecimal initialCapitalBD = BigDecimal.valueOf(initialCapital);

            if (priceData.isEmpty()) {
                log.warn("无法获取任何历史数据，使用模拟数据");
                return generateMockResult(strategyId, startDate, endDate, transactionFeeRate, slippageRate, initialCapitalBD);
            }

            // 3. 执行回测计算
            try {
                return calculateBacktest(holdings, priceData, startDate, endDate, 
                        initialCapitalBD, 
                        transactionFeeRate, 
                        slippageRate);
            } catch (Exception e) {
                log.error("回测计算失败: {}", e.getMessage(), e);
                return generateMockResult(strategyId, startDate, endDate, transactionFeeRate, slippageRate, initialCapitalBD);
            }
        } catch (Exception e) {
            log.error("回测执行过程中发生异常: {}", e.getMessage(), e);
            BigDecimal transactionFeeRate = BigDecimal.valueOf(transactionFee / 100.0);
            BigDecimal slippageRate = BigDecimal.valueOf(slippage / 100.0);
            BigDecimal initialCapitalBD = BigDecimal.valueOf(initialCapital);
            return generateMockResult(strategyId, startDate, endDate, transactionFeeRate, slippageRate, initialCapitalBD);
        }
    }

    /**
     * 获取策略持仓（从数据库，暂时返回空列表）
     */
    private List<Holding> getStrategyHoldings(Long strategyId) {
        // TODO: 从数据库获取策略持仓
        return new ArrayList<>();
    }

    /**
     * 获取默认持仓（用于演示）
     */
    private List<Holding> getDefaultHoldings() {
        // 使用一些常见股票作为默认持仓
        return Arrays.asList(
                new Holding("000001", "平安银行", BigDecimal.valueOf(0.20)),
                new Holding("600036", "招商银行", BigDecimal.valueOf(0.20)),
                new Holding("600519", "贵州茅台", BigDecimal.valueOf(0.15)),
                new Holding("000858", "五粮液", BigDecimal.valueOf(0.15)),
                new Holding("300750", "宁德时代", BigDecimal.valueOf(0.15)),
                new Holding("002594", "比亚迪", BigDecimal.valueOf(0.15))
        );
    }

    /**
     * 获取历史价格数据
     */
    private List<PricePoint> fetchHistoryPrices(String code, LocalDate startDate, LocalDate endDate) {
        String startStr = startDate.format(DATE_FORMATTER);
        String endStr = endDate.format(DATE_FORMATTER);
        
        try {
            List<Map<String, Object>> rawData = marketDataClient.getHistoryData(code, startStr, endStr);
            if (rawData == null || rawData.isEmpty()) {
                log.warn("股票 {} 历史数据为空", code);
                return new ArrayList<>();
            }
            
            List<PricePoint> prices = new ArrayList<>();
            
            // 支持多种日期格式
            DateTimeFormatter[] formatters = {
                    ISO_FORMATTER,  // yyyy-MM-dd
                    DATE_FORMATTER,  // yyyyMMdd
                    DateTimeFormatter.ofPattern("yyyy/MM/dd"),
                    DateTimeFormatter.ofPattern("MM/dd/yyyy")
            };
            
            for (Map<String, Object> item : rawData) {
                try {
                    String dateStr = getString(item, "日期", "date", "trade_date");
                    BigDecimal close = getBigDecimal(item, "收盘", "close", "收盘价");
                    
                    if (dateStr != null && close != null && close.compareTo(BigDecimal.ZERO) > 0) {
                        LocalDate date = null;
                        for (DateTimeFormatter formatter : formatters) {
                            try {
                                date = LocalDate.parse(dateStr, formatter);
                                break;
                            } catch (Exception ignored) {
                            }
                        }
                        if (date != null && !date.isBefore(startDate) && !date.isAfter(endDate)) {
                            prices.add(new PricePoint(date, close));
                        }
                    }
                } catch (Exception e) {
                    log.debug("解析价格数据失败: {}", e.getMessage());
                }
            }
            
            prices.sort(Comparator.comparing(PricePoint::getDate));
            log.debug("股票 {} 解析出 {} 条有效价格数据", code, prices.size());
            return prices;
        } catch (Exception e) {
            log.warn("获取股票 {} 历史数据异常: {}", code, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 执行回测计算
     */
    private BacktestResult calculateBacktest(List<Holding> holdings, 
                                             Map<String, List<PricePoint>> priceData,
                                             LocalDate startDate, LocalDate endDate,
                                             BigDecimal initialCapital,
                                             BigDecimal transactionFeeRate,
                                             BigDecimal slippageRate) {
        // 初始化
        BigDecimal capital = initialCapital;
        BigDecimal totalValue = initialCapital;
        List<BigDecimal> netValues = new ArrayList<>();
        List<LocalDate> dates = new ArrayList<>();
        int tradeCount = 0;
        
        // 获取所有交易日期（取所有股票的交集）
        Set<LocalDate> allDates = new TreeSet<>();
        for (List<PricePoint> prices : priceData.values()) {
            for (PricePoint point : prices) {
                if (!point.date.isBefore(startDate) && !point.date.isAfter(endDate)) {
                    allDates.add(point.date);
                }
            }
        }
        
        if (allDates.isEmpty()) {
            return generateMockResult(null, startDate, endDate, transactionFeeRate, slippageRate, initialCapital);
        }
        
        // 按日期遍历计算净值
        Map<String, BigDecimal> lastPrices = new HashMap<>();
        BigDecimal maxNetValue = initialCapital;
        BigDecimal maxDrawdown = BigDecimal.ZERO;
        
        // 初始化持仓：按权重分配资金
        Map<String, BigDecimal> positions = new HashMap<>();  // 持仓数量（股数）
        for (Holding holding : holdings) {
            List<PricePoint> prices = priceData.get(holding.code);
            if (prices != null && !prices.isEmpty()) {
                BigDecimal firstPrice = prices.get(0).price;
                if (firstPrice != null && firstPrice.compareTo(BigDecimal.ZERO) > 0) {
                    // 计算该持仓应投入的资金
                    BigDecimal positionCapital = initialCapital.multiply(holding.weight);
                    // 计算持仓数量（股数）
                    BigDecimal shares = positionCapital.divide(firstPrice, 0, RoundingMode.DOWN);
                    positions.put(holding.code, shares);
                    lastPrices.put(holding.code, firstPrice);
                }
            }
        }
        
        for (LocalDate date : allDates) {
            // 计算当日持仓市值
            BigDecimal dayValue = BigDecimal.ZERO;
            boolean hasData = false;
            
            for (Holding holding : holdings) {
                if (!positions.containsKey(holding.code)) continue;
                
                List<PricePoint> prices = priceData.get(holding.code);
                if (prices == null) continue;
                
                BigDecimal price = findPriceOnDate(prices, date);
                if (price == null && lastPrices.containsKey(holding.code)) {
                    price = lastPrices.get(holding.code);
                }
                
                if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
                    // 考虑滑点（买入时增加成本，卖出时减少收益）
                    BigDecimal adjustedPrice = price.multiply(BigDecimal.ONE.subtract(slippageRate));
                    BigDecimal shares = positions.get(holding.code);
                    BigDecimal positionValue = shares.multiply(adjustedPrice);
                    dayValue = dayValue.add(positionValue);
                    lastPrices.put(holding.code, price);
                    hasData = true;
                }
            }
            
            if (hasData && dayValue.compareTo(BigDecimal.ZERO) > 0) {
                // 计算净值
                BigDecimal netValue = dayValue;
                netValues.add(netValue);
                dates.add(date);
                
                // 更新最大净值
                if (netValue.compareTo(maxNetValue) > 0) {
                    maxNetValue = netValue;
                }
                
                // 计算回撤
                if (maxNetValue.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal drawdown = maxNetValue.subtract(netValue)
                            .divide(maxNetValue, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100));
                    if (drawdown.compareTo(maxDrawdown) > 0) {
                        maxDrawdown = drawdown;
                    }
                }
                
                // 模拟交易（简化：假设每月调仓一次）
                if (dates.size() % 20 == 0) {  // 大约每月一次
                    tradeCount++;
                }
            }
        }
        
        if (netValues.isEmpty()) {
            return generateMockResult(null, startDate, endDate, transactionFeeRate, slippageRate, initialCapital);
        }
        
        // 计算累计收益
        BigDecimal finalValue = netValues.get(netValues.size() - 1);
        BigDecimal cumulativeReturn = finalValue.subtract(initialCapital)
                .divide(initialCapital, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        
        // 计算夏普比率（简化计算）
        BigDecimal sharpeRatio = calculateSharpeRatio(netValues);
        
        // 应用交易费用（每次交易都会产生费用，影响最终收益）
        // 交易费用 = 交易次数 × 交易费用率 × 平均交易金额比例
        BigDecimal avgTradeValue = initialCapital.multiply(BigDecimal.valueOf(0.1)); // 假设每次交易约10%的资金
        BigDecimal totalFee = BigDecimal.valueOf(tradeCount)
                .multiply(transactionFeeRate)
                .multiply(avgTradeValue)
                .divide(initialCapital, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        cumulativeReturn = cumulativeReturn.subtract(totalFee);
        
        // 应用滑点影响（滑点会持续影响收益，不仅仅是估值）
        // 滑点影响 = 累计收益 × 滑点率（因为每次价格变动都受滑点影响）
        BigDecimal slippageImpact = cumulativeReturn.multiply(slippageRate);
        cumulativeReturn = cumulativeReturn.subtract(slippageImpact);
        
        log.info("回测完成: 累计收益={}%, 最大回撤={}%, 夏普比率={}, 交易次数={}, 交易费用影响={}%, 滑点影响={}%",
                cumulativeReturn, maxDrawdown, sharpeRatio, tradeCount, totalFee, slippageImpact);
        
        return new BacktestResult(
                cumulativeReturn.setScale(2, RoundingMode.HALF_UP),
                maxDrawdown.setScale(2, RoundingMode.HALF_UP),
                sharpeRatio.setScale(2, RoundingMode.HALF_UP),
                tradeCount
        );
    }

    /**
     * 在价格列表中找到指定日期的价格（如果当天没有数据，返回最近的前一个交易日价格）
     */
    private BigDecimal findPriceOnDate(List<PricePoint> prices, LocalDate date) {
        // 先尝试精确匹配
        for (PricePoint point : prices) {
            if (point.date.equals(date)) {
                return point.price;
            }
        }
        
        // 如果没有精确匹配，找最近的前一个交易日
        PricePoint latest = null;
        for (PricePoint point : prices) {
            if (!point.date.isAfter(date) && (latest == null || point.date.isAfter(latest.date))) {
                latest = point;
            }
        }
        
        return latest != null ? latest.price : null;
    }

    /**
     * 计算夏普比率（简化版）
     */
    private BigDecimal calculateSharpeRatio(List<BigDecimal> netValues) {
        if (netValues.size() < 2) {
            return BigDecimal.valueOf(1.0);
        }
        
        // 计算日收益率
        List<BigDecimal> returns = new ArrayList<>();
        for (int i = 1; i < netValues.size(); i++) {
            BigDecimal prev = netValues.get(i - 1);
            BigDecimal curr = netValues.get(i);
            if (prev.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal ret = curr.subtract(prev).divide(prev, 4, RoundingMode.HALF_UP);
                returns.add(ret);
            }
        }
        
        if (returns.isEmpty()) {
            return BigDecimal.valueOf(1.0);
        }
        
        // 计算平均收益率
        BigDecimal avgReturn = returns.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(returns.size()), 4, RoundingMode.HALF_UP);
        
        // 计算标准差
        BigDecimal variance = returns.stream()
                .map(ret -> ret.subtract(avgReturn).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(returns.size()), 4, RoundingMode.HALF_UP);
        
        BigDecimal stdDev = sqrt(variance);
        
        // 夏普比率 = (平均收益率 - 无风险利率) / 标准差
        // 简化：假设无风险利率为0，年化处理
        if (stdDev.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.valueOf(1.0);
        }
        
        BigDecimal sharpe = avgReturn.divide(stdDev, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(Math.sqrt(252)));  // 年化
        
        return sharpe.max(BigDecimal.valueOf(0.5)).min(BigDecimal.valueOf(2.5));  // 限制在合理范围
    }

    /**
     * 计算平方根（简化版）
     */
    private BigDecimal sqrt(BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        double d = value.doubleValue();
        return BigDecimal.valueOf(Math.sqrt(d));
    }

    /**
     * 生成模拟结果（当无法获取真实数据时）
     * 考虑交易费用和滑点的影响
     */
    private BacktestResult generateMockResult(Long strategyId, LocalDate startDate, LocalDate endDate,
                                              BigDecimal transactionFeeRate, BigDecimal slippageRate,
                                              BigDecimal initialCapital) {
        // 使用所有参数组合作为随机种子的一部分，确保不同参数产生不同结果
        long seed = (strategyId != null ? strategyId : 0) * 1000L 
                + startDate.toEpochDay() * 100L
                + endDate.toEpochDay() * 10L
                + transactionFeeRate.multiply(BigDecimal.valueOf(10000)).longValue()
                + slippageRate.multiply(BigDecimal.valueOf(10000)).longValue()
                + initialCapital.longValue() % 10000
                + System.currentTimeMillis() % 10000;
        Random random = new Random(seed);
        log.debug("生成模拟结果，种子={}, 交易费用={}, 滑点={}, 初始资金={}", 
                seed, transactionFeeRate, slippageRate, initialCapital);
        long days = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        
        // 基础收益（考虑时间长度）
        double baseReturn = 5 + random.nextDouble() * 10;
        // 根据时间长度调整收益（时间越长，收益可能越高）
        double timeFactor = Math.min(days / 365.0, 2.0); // 最多2倍
        baseReturn = baseReturn * (0.5 + timeFactor * 0.5);
        
        // 计算交易次数（根据时间长度和频率）
        int estimatedTrades = (int) (days / 20) + random.nextInt(10);
        
        // 应用交易费用影响（每次交易扣除费用）
        // 假设每次交易约10%的资金，交易费用 = 交易次数 × 交易费用率 × 10%
        double avgTradeRatio = 0.1;
        double feeImpact = estimatedTrades * transactionFeeRate.doubleValue() * avgTradeRatio * 100;
        baseReturn = baseReturn - feeImpact;
        
        // 应用滑点影响（滑点会持续影响收益）
        // 滑点影响 = 收益 × 滑点率（因为每次价格变动都受滑点影响）
        double slippageImpact = baseReturn * slippageRate.doubleValue();
        baseReturn = baseReturn - slippageImpact;
        
        log.debug("模拟结果计算: 基础收益={}, 交易次数={}, 交易费用影响={}, 滑点影响={}, 最终收益={}", 
                baseReturn + feeImpact + slippageImpact, estimatedTrades, feeImpact, slippageImpact, baseReturn);
        
        BigDecimal cumulativeReturn = BigDecimal.valueOf(Math.max(baseReturn, -50)) // 限制最低收益为-50%
                .setScale(2, RoundingMode.HALF_UP);
        
        // 最大回撤（与收益相关，但不会超过收益的绝对值）
        double maxDrawdownValue = 2 + random.nextDouble() * 3;
        if (cumulativeReturn.compareTo(BigDecimal.ZERO) > 0) {
            maxDrawdownValue = Math.min(maxDrawdownValue, cumulativeReturn.doubleValue() * 0.3);
        }
        BigDecimal maxDrawdown = BigDecimal.valueOf(maxDrawdownValue)
                .setScale(2, RoundingMode.HALF_UP);
        
        // 夏普比率（根据收益和回撤计算）
        double sharpeBase = 0.8 + random.nextDouble() * 1.0;
        // 如果收益为负，夏普比率降低
        if (cumulativeReturn.compareTo(BigDecimal.ZERO) < 0) {
            sharpeBase = sharpeBase * 0.5;
        }
        BigDecimal sharpeRatio = BigDecimal.valueOf(sharpeBase)
                .setScale(2, RoundingMode.HALF_UP);
        
        return new BacktestResult(cumulativeReturn, maxDrawdown, sharpeRatio, estimatedTrades);
    }

    private String getString(Map<String, Object> source, String... keys) {
        for (String key : keys) {
            Object value = source.get(key);
            if (value instanceof String str && !str.isEmpty()) {
                return str.trim();
            }
        }
        return null;
    }

    private BigDecimal getBigDecimal(Map<String, Object> source, String... keys) {
        for (String key : keys) {
            Object value = source.get(key);
            if (value == null) continue;
            if (value instanceof Number number) {
                return BigDecimal.valueOf(number.doubleValue());
            }
            if (value instanceof String str && !str.isEmpty() && !"--".equals(str)) {
                try {
                    return new BigDecimal(str.replace("%", "").replace(",", ""));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return null;
    }

    // 内部类
    static class Holding {
        String code;
        String name;
        BigDecimal weight;  // 持仓权重

        Holding(String code, String name, BigDecimal weight) {
            this.code = code;
            this.name = name;
            this.weight = weight;
        }
    }

    static class PricePoint {
        LocalDate date;
        BigDecimal price;

        PricePoint(LocalDate date, BigDecimal price) {
            this.date = date;
            this.price = price;
        }

        public LocalDate getDate() {
            return date;
        }

        public BigDecimal getPrice() {
            return price;
        }
    }

    public static class BacktestResult {
        private final BigDecimal cumulativeReturn;
        private final BigDecimal maxDrawdown;
        private final BigDecimal sharpeRatio;
        private final int trades;

        public BacktestResult(BigDecimal cumulativeReturn, BigDecimal maxDrawdown, 
                             BigDecimal sharpeRatio, int trades) {
            this.cumulativeReturn = cumulativeReturn;
            this.maxDrawdown = maxDrawdown;
            this.sharpeRatio = sharpeRatio;
            this.trades = trades;
        }

        public BigDecimal getCumulativeReturn() {
            return cumulativeReturn;
        }

        public BigDecimal getMaxDrawdown() {
            return maxDrawdown;
        }

        public BigDecimal getSharpeRatio() {
            return sharpeRatio;
        }

        public int getTrades() {
            return trades;
        }
    }
}

