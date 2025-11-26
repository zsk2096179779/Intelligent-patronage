package com.example.backend.service;

import com.example.backend.entity.Strategy;
import com.example.backend.entity.StrategyFactor;
import com.example.backend.entity.StrategyFilterRule;
import com.example.backend.mapper.StrategyMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class StrategyService {

    private final StrategyMapper strategyMapper;
    private final StrategyFactorService factorService;
    private final StrategyFilterRuleService filterRuleService;
    private final StrategyConfigService configService;

    public StrategyService(StrategyMapper strategyMapper,
                          StrategyFactorService factorService,
                          StrategyFilterRuleService filterRuleService,
                          StrategyConfigService configService) {
        this.strategyMapper = strategyMapper;
        this.factorService = factorService;
        this.filterRuleService = filterRuleService;
        this.configService = configService;
    }

    @Transactional
    public Strategy createStrategy(String name, String type, String description, Integer riskLevel, 
                                   Long owner, Map<String, Object> payload) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("策略名称不能为空");
        }
        if (!StringUtils.hasText(type)) {
            throw new IllegalArgumentException("策略类型不能为空");
        }

        LocalDateTime now = LocalDateTime.now();
        Strategy strategy = new Strategy();
        strategy.setName(name);
        strategy.setType(type);
        strategy.setDescription(description);
        strategy.setRiskLevel(riskLevel != null ? riskLevel : 3);  // 默认风险等级为3
        strategy.setStatus("paused");  // 新创建的策略默认为审核中状态
        strategy.setOwner(owner);  // 设置策略拥有者
        strategy.setCreatedAt(now);
        strategy.setUpdatedAt(now);

        // 保存策略基本信息
        strategyMapper.insert(strategy);
        
        // 创建默认的再平衡配置
        configService.getOrCreate(strategy.getId());
        
        // 保存因子配置
        if (payload != null) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> factorsData = (List<Map<String, Object>>) payload.get("factors");
            if (factorsData != null && !factorsData.isEmpty()) {
                List<StrategyFactor> factors = new ArrayList<>();
                for (Map<String, Object> factorData : factorsData) {
                    StrategyFactor factor = new StrategyFactor();
                    factor.setFactorName((String) factorData.get("factorName"));
                    Object weightObj = factorData.get("weight");
                    if (weightObj != null) {
                        BigDecimal weight = weightObj instanceof Number 
                            ? BigDecimal.valueOf(((Number) weightObj).doubleValue())
                            : new BigDecimal(weightObj.toString());
                        factor.setWeight(weight);
                    } else {
                        factor.setWeight(BigDecimal.ZERO);
                    }
                    factor.setFrequency((String) factorData.get("frequency"));
                    factors.add(factor);
                }
                factorService.saveFactors(strategy.getId(), factors);
            }
            
            // 保存选基规则 - 新结构：每个策略只有一条选基规则记录
            @SuppressWarnings("unchecked")
            Map<String, Object> filterRulesData = (Map<String, Object>) payload.get("filterRules");
            if (filterRulesData != null) {
                StrategyFilterRule rule = new StrategyFilterRule();
                
                // 处理 topN
                Object topNObj = filterRulesData.get("topN");
                if (topNObj != null) {
                    Integer topN = topNObj instanceof Number 
                        ? ((Number) topNObj).intValue()
                        : Integer.parseInt(topNObj.toString());
                    rule.setTopN(topN);
                }
                
                // 处理 typeLimit（7种类型独热编码，如 "1000000"）
                Object typeLimitObj = filterRulesData.get("typeLimit");
                if (typeLimitObj != null) {
                    rule.setTypeLimit(typeLimitObj.toString());
                }
                
                // 处理 scaleLimit
                Object scaleLimitObj = filterRulesData.get("scaleLimit");
                if (scaleLimitObj != null) {
                    Integer scaleLimit = scaleLimitObj instanceof Number 
                        ? ((Number) scaleLimitObj).intValue()
                        : Integer.parseInt(scaleLimitObj.toString());
                    rule.setScaleLimit(scaleLimit);
                }
                
                // 只有当至少有一个字段有值时才保存
                if (rule.getTopN() != null || rule.getTypeLimit() != null || rule.getScaleLimit() != null) {
                    List<StrategyFilterRule> rules = new ArrayList<>();
                    rules.add(rule);
                    filterRuleService.saveFilterRules(strategy.getId(), rules);
                }
            }
        }
        
        return strategy;
    }

    public List<Strategy> listAll() {
        return strategyMapper.findAll();
    }
    
    /**
     * 获取指定用户拥有的策略列表
     */
    public List<Strategy> listByOwner(Long ownerId) {
        return strategyMapper.findByOwner(ownerId);
    }

    public Optional<Strategy> findById(Long id) {
        return Optional.ofNullable(strategyMapper.findById(id));
    }

    @Transactional
    public boolean updateStatus(Long id, String status, Long owner) {
        return strategyMapper.updateStatus(id, status, owner) > 0;
    }

    @Transactional
    public boolean update(Strategy strategy) {
        strategy.setUpdatedAt(LocalDateTime.now());
        return strategyMapper.update(strategy) > 0;
    }

    @Transactional
    public boolean deleteById(Long id, Long owner) {
        // 删除策略时，关联的因子、选基规则、配置会通过外键级联删除
        // 只有策略拥有者才能删除
        return strategyMapper.deleteById(id, owner) > 0;
    }
    
    /**
     * 验证用户是否为策略拥有者
     */
    public boolean isOwner(Long strategyId, Long userId) {
        Optional<Strategy> optional = findById(strategyId);
        if (optional.isEmpty()) {
            return false;
        }
        Strategy strategy = optional.get();
        return strategy.getOwner() != null && strategy.getOwner().equals(userId);
    }

    public List<StrategyFactor> getFactorsByStrategyId(Long strategyId) {
        return factorService.getFactorsByStrategyId(strategyId);
    }

    public List<StrategyFilterRule> getFilterRulesByStrategyId(Long strategyId) {
        return filterRuleService.getFilterRulesByStrategyId(strategyId);
    }
}
