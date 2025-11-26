package com.example.backend.service;

import com.example.backend.dto.MarketQuote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class MarketDataClient {

    private static final Logger log = LoggerFactory.getLogger(MarketDataClient.class);

    private final RestTemplate restTemplate;

    @Value("${akshare.base-url:http://127.0.0.1:5001}")
    private String baseUrl;

    @Value("${akshare.backup-url:}")
    private String backupUrl;

    public MarketDataClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<MarketQuote> getRealtimeQuotes() {
        ApiResponse resp = invokeApi(buildUrl(baseUrl));
        List<Map<String, Object>> raw = resp.data != null ? resp.data : List.of();
        String dataSource = resp.source != null ? resp.source : "unknown";
        
        if (raw.isEmpty() && StringUtils.hasText(backupUrl)) {
            log.warn("主行情源无数据，尝试备用地址：{}", backupUrl);
            ApiResponse backupResp = invokeApi(buildUrl(backupUrl));
            raw = backupResp.data != null ? backupResp.data : List.of();
            dataSource = backupResp.source != null ? backupResp.source : "backup";
        }
        
        List<MarketQuote> quotes = new ArrayList<>();
        for (Map<String, Object> item : raw) {
            MarketQuote quote = toMarketQuote(item);
            if (quote != null) {
                quotes.add(quote);
            }
        }
        log.info("行情接口返回 {} 条记录 (数据来源: {})", quotes.size(), dataSource);
        return quotes;
    }

    /**
     * 获取历史K线数据
     * @param symbol 股票代码（如 "000001"）
     * @param startDate 开始日期（格式：yyyyMMdd）
     * @param endDate 结束日期（格式：yyyyMMdd）
     * @return 历史K线数据列表
     */
    public List<Map<String, Object>> getHistoryData(String symbol, String startDate, String endDate) {
        String url = buildHistoryUrl(baseUrl, symbol, startDate, endDate);
        try {
            log.debug("请求历史数据: {}", url);
            ApiResponse resp = invokeApi(url);
            List<Map<String, Object>> raw = resp.data != null ? resp.data : List.of();
            log.info("获取历史数据: {} 从 {} 到 {}, 返回 {} 条记录", symbol, startDate, endDate, raw.size());
            return raw;
        } catch (Exception ex) {
            log.warn("获取历史数据失败: {}, 尝试备用地址", ex.getMessage());
            if (StringUtils.hasText(backupUrl)) {
                try {
                    String backupUrlFull = buildHistoryUrl(this.backupUrl, symbol, startDate, endDate);
                    ApiResponse resp = invokeApi(backupUrlFull);
                    List<Map<String, Object>> raw = resp.data != null ? resp.data : List.of();
                    log.info("从备用地址获取历史数据: {} 从 {} 到 {}, 返回 {} 条记录", symbol, startDate, endDate, raw.size());
                    return raw;
                } catch (Exception backupEx) {
                    log.error("备用地址也失败: {}", backupEx.getMessage());
                }
            }
            // 不抛出异常，返回空列表，让回测引擎使用模拟数据
            log.warn("无法获取历史数据，返回空列表");
            return List.of();
        }
    }

    private String buildUrl(String base) {
        return base.endsWith("/") ? base + "api/stock/realtime" : base + "/api/stock/realtime";
    }

    private String buildHistoryUrl(String base, String symbol, String startDate, String endDate) {
        String endpoint = base.endsWith("/") ? base + "api/stock/history/" : base + "/api/stock/history/";
        return endpoint + symbol + "?start_date=" + startDate + "&end_date=" + endDate;
    }

    private ApiResponse invokeApi(String url) {
        try {
            var resp = restTemplate.getForObject(url, ApiResponse.class);
            if (resp == null || resp.code != 200) {
                throw new IllegalStateException("调用行情接口失败：" + (resp != null ? resp.message : "无响应"));
            }
            return resp;
        } catch (RestClientException ex) {
            throw new IllegalStateException("行情服务不可用：" + ex.getMessage(), ex);
        }
    }

    private MarketQuote toMarketQuote(Map<String, Object> source) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        MarketQuote quote = new MarketQuote();
        // 代码字段：支持 Tushare 和 akshare 格式
        quote.setCode(getString(source, "代码", "symbol", "code", "股票代码", "f12", "ts_code"));
        // 名称字段：如果名称是代码格式（如 "000001.SZ"），尝试提取纯代码
        String name = getString(source, "名称", "name", "股票简称", "f14");
        if (name != null && name.contains(".")) {
            // 如果名称是 ts_code 格式，尝试从股票列表获取真实名称，否则使用代码部分
            name = name.split("\\.")[0];
        }
        quote.setName(name);
        // 价格字段：支持 close（Tushare）和其他格式
        quote.setPrice(getBigDecimal(source, "最新价", "price", "close", "f2", "收盘价"));
        // 涨跌幅字段：支持 pct_chg（Tushare）和其他格式
        quote.setChangePercent(getBigDecimal(source, "涨跌幅", "changePercent", "涨幅", "pct_chg", "f3"));
        // 成交额字段：akshare 返回的是"成交额(万)"（万元），Tushare 返回的是元
        // 优先使用"成交额(万)"字段（akshare），如果没有则使用"成交额"并判断单位
        BigDecimal amount = getBigDecimal(source, "成交额(万)", "f6");
        if (amount == null) {
            amount = getBigDecimal(source, "成交额", "amount");
            // 如果成交额很大（> 10000），可能是以元为单位，转换为万元
            if (amount != null && amount.compareTo(BigDecimal.valueOf(10000)) > 0) {
                amount = amount.divide(BigDecimal.valueOf(10000), 2, java.math.RoundingMode.HALF_UP);
            }
        }
        quote.setAmount(amount);
        quote.setRank(getInteger(source, "序号", "rank", "f0"));
        if (!StringUtils.hasText(quote.getCode()) && !StringUtils.hasText(quote.getName())) {
            return null;
        }
        return quote;
    }

    private String getString(Map<String, Object> source, String... keys) {
        for (String key : keys) {
            Object value = source.get(key);
            if (value instanceof String str && StringUtils.hasText(str)) {
                return str.trim();
            }
        }
        return null;
    }

    private BigDecimal getBigDecimal(Map<String, Object> source, String... keys) {
        for (String key : keys) {
            Object value = source.get(key);
            if (value == null) {
                continue;
            }
            if (value instanceof Number number) {
                return BigDecimal.valueOf(number.doubleValue());
            }
            if (value instanceof String str && StringUtils.hasText(str) && !"--".equals(str)) {
                try {
                    return new BigDecimal(str.replace("%", "").replace(",", ""));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return null;
    }

    private Integer getInteger(Map<String, Object> source, String... keys) {
        return Objects.requireNonNullElse(getBigDecimal(source, keys), BigDecimal.ZERO).intValue();
    }

    public static class ApiResponse {
        public int code;
        public String message;
        public List<Map<String, Object>> data;
        public String source;  // 数据来源：real/cache/fallback/expired_cache
        public Boolean cached;  // 是否来自缓存
    }
}

