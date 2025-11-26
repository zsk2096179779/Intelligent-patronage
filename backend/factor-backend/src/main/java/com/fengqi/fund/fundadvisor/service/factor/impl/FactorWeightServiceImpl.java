package com.fengqi.fund.fundadvisor.service.factor.impl;

import com.fengqi.fund.fundadvisor.dto.ResultDTO;
import com.fengqi.fund.fundadvisor.dto.factor.WeightConfigRequest;
import com.fengqi.fund.fundadvisor.dto.factor.WeightConfigResponse;
import com.fengqi.fund.fundadvisor.dto.factor.WeightValidationResult;
import com.fengqi.fund.fundadvisor.entity.factor.FactorBase;
import com.fengqi.fund.fundadvisor.entity.factor.FactorWeight;
import com.fengqi.fund.fundadvisor.mapper.factor.FactorBaseMapper;
import com.fengqi.fund.fundadvisor.mapper.factor.FactorWeightMapper;
import com.fengqi.fund.fundadvisor.service.factor.FactorWeightService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 因子权重服务实现类
 * 
 * @author fund-advisor
 * @since 2025-11-21
 */
@Slf4j
@Service
public class FactorWeightServiceImpl implements FactorWeightService {
    
    @Autowired
    private FactorWeightMapper factorWeightMapper;
    
    @Autowired
    private FactorBaseMapper factorBaseMapper;
    
    @Override
    @Transactional
    public ResultDTO<WeightConfigResponse> configureWeights(WeightConfigRequest request) {
        try {
            // 参数校验
            if (request == null || request.getFactorWeights() == null || request.getFactorWeights().isEmpty()) {
                return ResultDTO.error("权重配置不能为空");
            }
            
            // 验证衍生因子是否存在
            FactorBase derivedFactor = factorBaseMapper.getFactorById(request.getDerivedFactorId());
            if (derivedFactor == null) {
                return ResultDTO.error("指定的衍生因子不存在");
            }
            
            // 验证权重配置
            ResultDTO<WeightValidationResult> validationResult = validateWeights(request);
            if (!validationResult.getData().getValid()) {
                return ResultDTO.error("权重配置验证失败：" + validationResult.getData().getMessage());
            }
            
            // 删除原有权重配置
            factorWeightMapper.deleteWeightsByDerivedFactor(request.getDerivedFactorId());
            
            // 处理权重归一化
            List<WeightConfigRequest.FactorWeightItem> processedWeights = processWeightNormalization(request);
            
            // 创建权重实体列表
            List<FactorWeight> factorWeights = createWeightEntities(request.getDerivedFactorId(), processedWeights);
            
            // 批量插入权重配置
            int affectedRows = factorWeightMapper.batchInsertWeights(factorWeights);
            if (affectedRows != processedWeights.size()) {
                throw new RuntimeException("权重配置保存失败");
            }
            
            // 返回配置结果
            WeightConfigResponse response = buildWeightConfigResponse(request.getDerivedFactorId(), processedWeights);
            
            log.info("成功配置权重，衍生因子ID: {}, 权重项数: {}", request.getDerivedFactorId(), processedWeights.size());
            return ResultDTO.success(response, "权重配置成功");
            
        } catch (Exception e) {
            log.error("配置因子权重失败", e);
            return ResultDTO.error("配置权重失败：" + e.getMessage());
        }
    }
    
    @Override
    public ResultDTO<WeightConfigResponse> getWeightConfig(Integer derivedFactorId) {
        try {
            if (derivedFactorId == null) {
                return ResultDTO.error("衍生因子ID不能为空");
            }
            
            List<FactorWeight> weights = factorWeightMapper.getWeightsByDerivedFactor(derivedFactorId);
            FactorBase derivedFactor = factorBaseMapper.getFactorById(derivedFactorId);
            
            if (derivedFactor == null) {
                return ResultDTO.error("指定的衍生因子不存在");
            }
            
            WeightConfigResponse response = new WeightConfigResponse();
            response.setDerivedFactorId(derivedFactorId);
            response.setDerivedFactorName(derivedFactor.getFactorName());
            
            if (weights != null && !weights.isEmpty()) {
                List<WeightConfigResponse.FactorWeightDetail> weightDetails = weights.stream()
                    .map(this::convertToWeightDetail)
                    .collect(Collectors.toList());
                
                response.setFactorWeights(weightDetails);
                response.setTotalWeight(weights.stream()
                    .filter(w -> w.getIsEnabled() == 1)
                    .mapToDouble(FactorWeight::getWeight)
                    .sum());
                response.setIsNormalized(Math.abs(response.getTotalWeight() - 1.0) < 0.0001);
                response.setWeightId(weights.get(0).getWeightId()); // 设置第一个权重ID作为标识
                response.setCreateTime(weights.get(0).getCreateTime());
                response.setUpdateTime(weights.get(0).getUpdateTime());
                response.setCreator(weights.get(0).getCreator());
                response.setRemark(weights.get(0).getRemark());
            }
            
            return ResultDTO.success(response);
            
        } catch (Exception e) {
            log.error("获取权重配置失败", e);
            return ResultDTO.error("获取权重配置失败：" + e.getMessage());
        }
    }
    
    @Override
    public ResultDTO<WeightValidationResult> validateWeights(WeightConfigRequest request) {
        try {
            WeightValidationResult result = new WeightValidationResult();
            List<WeightValidationResult.ValidationError> errors = new ArrayList<>();
            List<WeightValidationResult.ValidationWarning> warnings = new ArrayList<>();
            
            if (request == null || request.getFactorWeights() == null) {
                result.setValid(false);
                result.setMessage("权重配置不能为空");
                return ResultDTO.success(result);
            }
            
            List<WeightConfigRequest.FactorWeightItem> weightItems = request.getFactorWeights();
            double totalWeight = 0.0;
            
            // 验证每个权重项
            for (WeightConfigRequest.FactorWeightItem item : weightItems) {
                // 检查基础因子是否存在
                FactorBase baseFactor = factorBaseMapper.getFactorById(item.getBaseFactorId());
                if (baseFactor == null) {
                    WeightValidationResult.ValidationError error = new WeightValidationResult.ValidationError();
                    error.setCode("FACTOR_NOT_EXISTS");
                    error.setMessage("基础因子ID " + item.getBaseFactorId() + " 不存在");
                    error.setBaseFactorId(item.getBaseFactorId());
                    errors.add(error);
                    continue;
                }
                
                // 验证权重值范围
                if (item.getWeight() < 0 || item.getWeight() > 1) {
                    WeightValidationResult.ValidationError error = new WeightValidationResult.ValidationError();
                    error.setCode("WEIGHT_OUT_OF_RANGE");
                    error.setMessage("权重值必须在0-1之间");
                    error.setBaseFactorId(item.getBaseFactorId());
                    error.setBaseFactorName(baseFactor.getFactorName());
                    error.setCurrentValue(item.getWeight());
                    error.setMaxValue(1.0);
                    errors.add(error);
                }
                
                // 检查权重是否过小
                if (item.getWeight() < 0.01) {
                    WeightValidationResult.ValidationWarning warning = new WeightValidationResult.ValidationWarning();
                    warning.setCode("WEIGHT_TOO_SMALL");
                    warning.setMessage("权重值过小，可能影响因子效果");
                    warning.setBaseFactorId(item.getBaseFactorId());
                    warning.setBaseFactorName(baseFactor.getFactorName());
                    warning.setCurrentValue(item.getWeight());
                    warning.setSuggestedMinValue(0.01);
                    warnings.add(warning);
                }
                
                if (Boolean.TRUE.equals(item.getEnabled())) {
                    totalWeight += item.getWeight();
                }
            }
            
            // 检查权重总和
            double threshold = request.getWeightSumThreshold() != null ? request.getWeightSumThreshold() : 1.0;
            if (Math.abs(totalWeight - threshold) > 0.0001) {
                WeightValidationResult.ValidationError error = new WeightValidationResult.ValidationError();
                error.setCode("WEIGHT_SUM_INVALID");
                error.setMessage("权重总和应为 " + threshold + "，当前为 " + totalWeight);
                error.setCurrentValue(totalWeight);
                error.setMaxValue(threshold);
                errors.add(error);
            }
            
            result.setValid(errors.isEmpty());
            result.setTotalWeight(totalWeight);
            result.setErrors(errors);
            result.setWarnings(warnings);
            result.setMessage(errors.isEmpty() ? "权重配置验证通过" : "权重配置存在错误");
            
            return ResultDTO.success(result);
            
        } catch (Exception e) {
            log.error("验证权重配置失败", e);
            return ResultDTO.error("验证权重配置失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public ResultDTO<WeightConfigResponse> normalizeWeights(WeightConfigRequest request) {
        try {
            if (request == null || request.getFactorWeights() == null) {
                return ResultDTO.error("权重配置不能为空");
            }
            
            // 计算当前权重总和
            double totalWeight = request.getFactorWeights().stream()
                .filter(item -> Boolean.TRUE.equals(item.getEnabled()))
                .mapToDouble(WeightConfigRequest.FactorWeightItem::getWeight)
                .sum();
            
            if (totalWeight <= 0) {
                return ResultDTO.error("权重总和必须大于0");
            }
            
            // 归一化权重
            List<WeightConfigRequest.FactorWeightItem> normalizedWeights = request.getFactorWeights().stream()
                .map(item -> {
                    WeightConfigRequest.FactorWeightItem normalizedItem = new WeightConfigRequest.FactorWeightItem();
                    normalizedItem.setBaseFactorId(item.getBaseFactorId());
                    normalizedItem.setEnabled(item.getEnabled());
                    if (Boolean.TRUE.equals(item.getEnabled())) {
                        normalizedItem.setWeight(item.getWeight() / totalWeight);
                    } else {
                        normalizedItem.setWeight(item.getWeight());
                    }
                    return normalizedItem;
                })
                .collect(Collectors.toList());
            
            // 创建新的请求对象
            WeightConfigRequest normalizedRequest = new WeightConfigRequest();
            normalizedRequest.setDerivedFactorId(request.getDerivedFactorId());
            normalizedRequest.setFactorWeights(normalizedWeights);
            normalizedRequest.setAutoNormalize(false); // 已经归一化，不需要再次归一化
            normalizedRequest.setRemark(request.getRemark() + "（已自动归一化）");
            
            // 保存归一化后的权重
            return configureWeights(normalizedRequest);
            
        } catch (Exception e) {
            log.error("归一化权重失败", e);
            return ResultDTO.error("归一化权重失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public ResultDTO<Boolean> updateSingleWeight(Integer weightId, Double weight) {
        try {
            if (weightId == null || weight == null) {
                return ResultDTO.error("参数不能为空");
            }
            
            if (weight < 0 || weight > 1) {
                return ResultDTO.error("权重值必须在0-1之间");
            }
            
            FactorWeight existingWeight = factorWeightMapper.getWeightById(weightId);
            if (existingWeight == null) {
                return ResultDTO.error("权重配置不存在");
            }
            
            existingWeight.setWeight(weight);
            existingWeight.setWeightPercentage(weight * 100);
            existingWeight.setUpdateTime(LocalDateTime.now());
            
            int affectedRows = factorWeightMapper.updateWeight(existingWeight);
            
            return affectedRows > 0 ? ResultDTO.success(true, "权重更新成功") : ResultDTO.error("权重更新失败");
            
        } catch (Exception e) {
            log.error("更新单个权重失败", e);
            return ResultDTO.error("更新权重失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public ResultDTO<Boolean> toggleWeightEnabled(Integer weightId, Boolean enabled) {
        try {
            if (weightId == null || enabled == null) {
                return ResultDTO.error("参数不能为空");
            }
            
            FactorWeight existingWeight = factorWeightMapper.getWeightById(weightId);
            if (existingWeight == null) {
                return ResultDTO.error("权重配置不存在");
            }
            
            existingWeight.setIsEnabled(enabled ? 1 : 0);
            existingWeight.setUpdateTime(LocalDateTime.now());
            
            int affectedRows = factorWeightMapper.updateWeight(existingWeight);
            
            return affectedRows > 0 ? ResultDTO.success(true, "状态更新成功") : ResultDTO.error("状态更新失败");
            
        } catch (Exception e) {
            log.error("更新权重状态失败", e);
            return ResultDTO.error("更新状态失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public ResultDTO<Boolean> deleteWeight(Integer weightId) {
        try {
            if (weightId == null) {
                return ResultDTO.error("权重ID不能为空");
            }
            
            int affectedRows = factorWeightMapper.deleteWeight(weightId);
            
            return affectedRows > 0 ? ResultDTO.success(true, "权重删除成功") : ResultDTO.error("权重删除失败");
            
        } catch (Exception e) {
            log.error("删除权重失败", e);
            return ResultDTO.error("删除权重失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public ResultDTO<WeightConfigResponse> batchUpdateWeights(Integer derivedFactorId, 
                                                            List<WeightConfigRequest.FactorWeightItem> weightItems) {
        try {
            WeightConfigRequest request = new WeightConfigRequest();
            request.setDerivedFactorId(derivedFactorId);
            request.setFactorWeights(weightItems);
            request.setAutoNormalize(true);
            
            return configureWeights(request);
            
        } catch (Exception e) {
            log.error("批量更新权重失败", e);
            return ResultDTO.error("批量更新权重失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public ResultDTO<WeightConfigResponse> resetToEqualWeights(Integer derivedFactorId) {
        try {
            List<FactorWeight> existingWeights = factorWeightMapper.getWeightsByDerivedFactor(derivedFactorId);
            
            if (existingWeights == null || existingWeights.isEmpty()) {
                return ResultDTO.error("没有找到权重配置");
            }
            
            long enabledCount = existingWeights.stream()
                .filter(w -> w.getIsEnabled() == 1)
                .count();
            
            if (enabledCount == 0) {
                return ResultDTO.error("没有启用的权重项");
            }
            
            double equalWeight = 1.0 / enabledCount;
            
            List<WeightConfigRequest.FactorWeightItem> equalWeights = existingWeights.stream()
                .map(w -> {
                    WeightConfigRequest.FactorWeightItem item = new WeightConfigRequest.FactorWeightItem();
                    item.setBaseFactorId(w.getBaseFactorId());
                    item.setWeight(w.getIsEnabled() == 1 ? equalWeight : w.getWeight());
                    item.setEnabled(w.getIsEnabled() == 1);
                    return item;
                })
                .collect(Collectors.toList());
            
            WeightConfigRequest request = new WeightConfigRequest();
            request.setDerivedFactorId(derivedFactorId);
            request.setFactorWeights(equalWeights);
            request.setAutoNormalize(false);
            request.setRemark("重置为等权重");
            
            return configureWeights(request);
            
        } catch (Exception e) {
            log.error("重置为等权重失败", e);
            return ResultDTO.error("重置权重失败：" + e.getMessage());
        }
    }
    
    @Override
    public ResultDTO<Map<String, Object>> getWeightStatistics(Integer derivedFactorId) {
        try {
            List<FactorWeight> weights = factorWeightMapper.getWeightsByDerivedFactor(derivedFactorId);
            
            if (weights == null || weights.isEmpty()) {
                return ResultDTO.error("没有找到权重配置");
            }
            
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalWeights", weights.size());
            statistics.put("enabledWeights", weights.stream().mapToInt(w -> w.getIsEnabled()).sum());
            statistics.put("totalWeightSum", weights.stream()
                .filter(w -> w.getIsEnabled() == 1)
                .mapToDouble(FactorWeight::getWeight)
                .sum());
            statistics.put("maxWeight", weights.stream()
                .filter(w -> w.getIsEnabled() == 1)
                .mapToDouble(FactorWeight::getWeight)
                .max()
                .orElse(0.0));
            statistics.put("minWeight", weights.stream()
                .filter(w -> w.getIsEnabled() == 1)
                .mapToDouble(FactorWeight::getWeight)
                .min()
                .orElse(0.0));
            statistics.put("averageWeight", weights.stream()
                .filter(w -> w.getIsEnabled() == 1)
                .mapToDouble(FactorWeight::getWeight)
                .average()
                .orElse(0.0));
            
            return ResultDTO.success(statistics);
            
        } catch (Exception e) {
            log.error("获取权重统计失败", e);
            return ResultDTO.error("获取统计信息失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public ResultDTO<WeightConfigResponse> copyWeightConfig(Integer sourceDerivedFactorId, Integer targetDerivedFactorId) {
        try {
            List<FactorWeight> sourceWeights = factorWeightMapper.getWeightsByDerivedFactor(sourceDerivedFactorId);
            
            if (sourceWeights == null || sourceWeights.isEmpty()) {
                return ResultDTO.error("源因子没有权重配置");
            }
            
            // 删除目标因子的原有配置
            factorWeightMapper.deleteWeightsByDerivedFactor(targetDerivedFactorId);
            
            // 复制权重配置
            List<FactorWeight> targetWeights = sourceWeights.stream()
                .map(w -> {
                    FactorWeight newWeight = new FactorWeight();
                    newWeight.setDerivedFactorId(targetDerivedFactorId);
                    newWeight.setBaseFactorId(w.getBaseFactorId());
                    newWeight.setWeight(w.getWeight());
                    newWeight.setWeightPercentage(w.getWeightPercentage());
                    newWeight.setIsEnabled(w.getIsEnabled());
                    newWeight.setCreateTime(LocalDateTime.now());
                    newWeight.setUpdateTime(LocalDateTime.now());
                    newWeight.setRemark("从因子ID " + sourceDerivedFactorId + " 复制");
                    return newWeight;
                })
                .collect(Collectors.toList());
            
            int affectedRows = factorWeightMapper.batchInsertWeights(targetWeights);
            
            if (affectedRows != targetWeights.size()) {
                return ResultDTO.error("权重配置复制失败");
            }
            
            return getWeightConfig(targetDerivedFactorId);
            
        } catch (Exception e) {
            log.error("复制权重配置失败", e);
            return ResultDTO.error("复制权重配置失败：" + e.getMessage());
        }
    }
    
    /**
     * 处理权重归一化
     */
    private List<WeightConfigRequest.FactorWeightItem> processWeightNormalization(WeightConfigRequest request) {
        if (!Boolean.TRUE.equals(request.getAutoNormalize())) {
            return request.getFactorWeights();
        }
        
        double totalWeight = request.getFactorWeights().stream()
            .filter(item -> Boolean.TRUE.equals(item.getEnabled()))
            .mapToDouble(WeightConfigRequest.FactorWeightItem::getWeight)
            .sum();
        
        if (totalWeight <= 0 || Math.abs(totalWeight - 1.0) < 0.0001) {
            return request.getFactorWeights();
        }
        
        return request.getFactorWeights().stream()
            .map(item -> {
                WeightConfigRequest.FactorWeightItem normalizedItem = new WeightConfigRequest.FactorWeightItem();
                normalizedItem.setBaseFactorId(item.getBaseFactorId());
                normalizedItem.setEnabled(item.getEnabled());
                if (Boolean.TRUE.equals(item.getEnabled())) {
                    normalizedItem.setWeight(item.getWeight() / totalWeight);
                } else {
                    normalizedItem.setWeight(item.getWeight());
                }
                return normalizedItem;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * 创建权重实体列表
     */
    private List<FactorWeight> createWeightEntities(Integer derivedFactorId, 
                                                   List<WeightConfigRequest.FactorWeightItem> weightItems) {
        LocalDateTime now = LocalDateTime.now();
        
        return weightItems.stream()
            .map(item -> {
                FactorWeight factorWeight = new FactorWeight();
                factorWeight.setDerivedFactorId(derivedFactorId);
                factorWeight.setBaseFactorId(item.getBaseFactorId());
                factorWeight.setWeight(item.getWeight());
                factorWeight.setWeightPercentage(item.getWeight() * 100);
                factorWeight.setIsEnabled(Boolean.TRUE.equals(item.getEnabled()) ? 1 : 0);
                factorWeight.setCreateTime(now);
                factorWeight.setUpdateTime(now);
                factorWeight.setCreator("system"); // 可以从SecurityContext获取当前用户
                factorWeight.setUpdater("system");
                return factorWeight;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * 构建权重配置响应
     */
    private WeightConfigResponse buildWeightConfigResponse(Integer derivedFactorId, 
                                                         List<WeightConfigRequest.FactorWeightItem> weightItems) {
        WeightConfigResponse response = new WeightConfigResponse();
        response.setDerivedFactorId(derivedFactorId);
        
        // 获取基础因子信息
        List<Integer> baseFactorIds = weightItems.stream()
            .map(WeightConfigRequest.FactorWeightItem::getBaseFactorId)
            .collect(Collectors.toList());
        
        Map<Integer, FactorBase> factorMap = factorBaseMapper.getFactorsByIds(baseFactorIds)
            .stream()
            .collect(Collectors.toMap(FactorBase::getBaseId, f -> f));
        
        List<WeightConfigResponse.FactorWeightDetail> weightDetails = weightItems.stream()
            .map(item -> {
                WeightConfigResponse.FactorWeightDetail detail = new WeightConfigResponse.FactorWeightDetail();
                detail.setBaseFactorId(item.getBaseFactorId());
                FactorBase factor = factorMap.get(item.getBaseFactorId());
                if (factor != null) {
                    detail.setBaseFactorName(factor.getFactorName());
                    detail.setBaseFactorCode(factor.getFactorCode());
                    detail.setDataSource(factor.getDataSource());
                }
                detail.setWeight(item.getWeight());
                detail.setWeightPercentage(item.getWeight() * 100);
                detail.setEnabled(Boolean.TRUE.equals(item.getEnabled()));
                return detail;
            })
            .collect(Collectors.toList());
        
        response.setFactorWeights(weightDetails);
        response.setTotalWeight(weightItems.stream()
            .filter(item -> Boolean.TRUE.equals(item.getEnabled()))
            .mapToDouble(WeightConfigRequest.FactorWeightItem::getWeight)
            .sum());
        response.setIsNormalized(Math.abs(response.getTotalWeight() - 1.0) < 0.0001);
        
        return response;
    }
    
    /**
     * 转换为权重详情
     */
    private WeightConfigResponse.FactorWeightDetail convertToWeightDetail(FactorWeight weight) {
        WeightConfigResponse.FactorWeightDetail detail = new WeightConfigResponse.FactorWeightDetail();
        detail.setWeightId(weight.getWeightId());
        detail.setBaseFactorId(weight.getBaseFactorId());
        detail.setBaseFactorName(weight.getBaseFactorId() != null ? "" : ""); // 从查询结果获取
        detail.setBaseFactorCode(weight.getBaseFactorId() != null ? "" : ""); // 从查询结果获取
        detail.setDataSource(weight.getBaseFactorId() != null ? "" : ""); // 从查询结果获取
        detail.setWeight(weight.getWeight());
        detail.setWeightPercentage(weight.getWeightPercentage());
        detail.setEnabled(weight.getIsEnabled() == 1);
        return detail;
    }
}