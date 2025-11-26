# 风险暴露矩阵实现说明

## 📋 功能概述

实现了基于策略因子配置的多维风险暴露矩阵计算和展示功能。系统会：
1. 读取策略配置的因子和权重
2. 从CSV文件读取基金因子数据
3. 计算标准化后的因子得分
4. 选出topN基金
5. 计算风险暴露矩阵（仓位、收益、回撤与基准的差值）
6. 在前端以热力图形式展示

---

## 🏗️ 架构设计

### 后端组件

#### 1. DTO类
- **`FundFactorData`**: 基金因子数据（从CSV解析）
  - 包含6个因子：dividend_ratio, volatility_20, momentum_20, drawdown, sharpe_20, mean_reversion
  - 包含综合得分 compositeScore

- **`RiskExposureMatrix`**: 风险暴露矩阵数据结构
  - `funds`: 基金列表（横轴）
  - `metrics`: 风险指标数据（纵轴：position, return, drawdown）
  - `baseline`: 基准值

#### 2. Service层
- **`FundFactorAnalysisService`**: 因子分析服务
  - `loadFundFactorsFromCsv()`: 读取CSV文件
  - `calculateTopNFunds()`: 计算topN基金
    - 使用Z-score标准化
    - 根据策略因子权重加权计算综合得分
    - 按得分降序排序，选出topN

- **`RiskExposureMatrixService`**: 风险暴露矩阵计算服务
  - `calculateRiskExposureMatrix()`: 计算风险暴露矩阵
    - 调用因子分析服务获取topN基金
    - 计算基准值（所有基金的平均值）
    - 计算仓位、收益、回撤与基准的差值

#### 3. Controller层
- **`StrategyMonitoringController`**: 新增端点
  - `POST /api/strategy-monitoring/RiskExposureMatrix`
  - 获取策略因子配置和筛选规则
  - 调用服务计算风险暴露矩阵
  - 返回JSON数据

### 前端组件

- **`StrategyMonitoring.vue`**: 策略监控页面
  - 新增 `initRiskExposureMatrixChart()` 函数
  - 使用ECharts热力图展示矩阵
  - 横轴：topN基金名称
  - 纵轴：仓位、收益、回撤
  - 颜色映射：白色（无偏离）→ 蓝色（严重偏离）

---

## 📊 数据流程

```
1. 前端请求
   POST /api/strategy-monitoring/RiskExposureMatrix
   { id: strategyId }

2. 后端处理
   ├─ 获取策略因子配置 (StrategyFactor)
   ├─ 获取筛选规则 (StrategyFilterRule) → topN
   ├─ 读取CSV文件 (fund_factors_result.csv)
   ├─ 标准化因子值 (Z-score)
   ├─ 加权计算综合得分
   ├─ 选出topN基金
   ├─ 计算基准值（所有基金平均值）
   └─ 计算风险暴露矩阵

3. 返回数据
   {
     success: true,
     data: {
       funds: [{code, name, score}],
       metrics: {
         position: [0.1, 0.1, ...],
         return: [0.05, -0.02, ...],
         drawdown: [-0.01, 0.03, ...]
       },
       baseline: {
         return: 0.08,
         drawdown: -0.05
       }
     }
   }

4. 前端展示
   └─ ECharts热力图
      - 横轴：基金名称
      - 纵轴：仓位/收益/回撤
      - 颜色：根据差值大小映射
```

---

## 🔧 核心算法

### 1. 因子标准化（Z-score）
```java
normalizedValue = (value - mean) / std
```

### 2. 综合得分计算
```java
compositeScore = Σ(normalizedFactorValue × weight) / Σ(weight)
```

### 3. 风险暴露指标计算
- **仓位**: 等权重分配，每个基金 = 1/topN
- **收益差值**: fundReturn - baselineReturn
  - fundReturn = navAcc - 1
- **回撤差值**: fundDrawdown - baselineDrawdown

---

## 📁 文件清单

### 后端新增文件
- `backend/src/main/java/com/example/backend/dto/FundFactorData.java`
- `backend/src/main/java/com/example/backend/dto/RiskExposureMatrix.java`
- `backend/src/main/java/com/example/backend/service/FundFactorAnalysisService.java`
- `backend/src/main/java/com/example/backend/service/RiskExposureMatrixService.java`

### 后端修改文件
- `backend/src/main/java/com/example/backend/controller/StrategyMonitoringController.java`
  - 新增 `riskExposureMatrix()` 方法

### 前端修改文件
- `frontend/src/pages/StrategyMonitoring.vue`
  - 新增 `initRiskExposureMatrixChart()` 函数
  - 修改 `loadMonitorData()` 函数，添加风险暴露矩阵请求

---

## 🎯 使用说明

### 前置条件
1. **CSV文件**: `fund_factors_result.csv` 必须存在于项目根目录
2. **策略配置**: 策略必须配置了因子（StrategyFactor）和筛选规则（StrategyFilterRule）
3. **因子名称**: 支持的因子名称：
   - `dividend_ratio` / `dividendratio`
   - `volatility_20` / `volatility20`
   - `momentum_20` / `momentum20`
   - `drawdown`
   - `sharpe_20` / `sharpe20`
   - `mean_reversion` / `meanreversion`

### API调用示例
```javascript
// 前端调用
const response = await http.post('/strategy-monitoring/RiskExposureMatrix', {
  id: strategyId
})

if (response.data.success) {
  const matrix = response.data.data
  // matrix.funds: 基金列表
  // matrix.metrics: 指标数据
  // matrix.baseline: 基准值
}
```

### 前端展示
风险暴露矩阵会在策略监控页面的"多维风险暴露矩阵"卡片中展示，以热力图形式呈现。

---

## ⚠️ 注意事项

1. **CSV文件路径**: CSV文件必须在项目根目录，文件名必须为 `fund_factors_result.csv`
2. **因子权重**: 如果因子权重为0或null，该因子不会参与计算
3. **topN默认值**: 如果筛选规则中没有配置topN，默认使用10
4. **数据缺失处理**: 如果某个基金的因子值缺失，该因子不参与该基金的综合得分计算
5. **基准值计算**: 基准值使用所有基金的平均值，如果CSV文件为空，基准值设为0

---

## 🔍 调试建议

1. **检查CSV文件**: 确认文件存在且格式正确
2. **检查策略配置**: 确认策略已配置因子和筛选规则
3. **查看日志**: 后端日志会输出CSV读取和计算过程
4. **前端控制台**: 查看浏览器控制台的网络请求和响应数据

---

## 📈 未来扩展

可以考虑的改进方向：
1. 支持更多因子类型
2. 支持自定义基准值（而非使用平均值）
3. 支持动态更新CSV文件
4. 添加因子相关性分析
5. 支持导出风险暴露矩阵报告

