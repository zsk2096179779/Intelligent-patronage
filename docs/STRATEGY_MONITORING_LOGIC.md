# 策略监控页逻辑说明

## 概述
策略监控页提供四个主要功能模块的数据展示：
1. **预警中心** - 显示策略相关的预警信息
2. **持仓偏离度热力图** - 展示不同行业在不同维度下的持仓偏离度
3. **预计策略收益曲线** - 展示策略净值随时间的变化曲线
4. **风险指标** - 显示策略的系统状态、风险等级、再平衡周期等指标

## 数据库表结构

### 1. strategy_warning (策略预警表)
```sql
CREATE TABLE `strategy_warning` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `strategy_id` bigint(20) NOT NULL,
  `title` varchar(255) NOT NULL DEFAULT '',
  `description` text,
  `event_time` datetime NOT NULL,
  `risk_level` varchar(20) NOT NULL DEFAULT 'low',
  `resolved` tinyint(1) NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_strategy_id` (`strategy_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 2. strategy_heatmap (持仓偏离度热力图表)
```sql
CREATE TABLE strategy_heatmap (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  strategy_id bigint(20) NOT NULL,
  industry varchar(100) NOT NULL,
  comparison_dimension varchar(100) NOT NULL,
  deviation_value decimal(10, 4) NOT NULL DEFAULT '0.0000',
  created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_strategy_id (strategy_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 3. strategy_profit_curve (策略收益曲线表)
```sql
CREATE TABLE strategy_profit_curve (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  strategy_id bigint(20) NOT NULL,
  point_date date NOT NULL,
  net_value decimal(16, 4) NOT NULL DEFAULT '1.0000',
  created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_strategy_date (strategy_id, point_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 4. strategy_metrics (策略风险监控指标表)
```sql
CREATE TABLE `strategy_metrics` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `strategy_id` bigint(20) NOT NULL,
  `system_status` varchar(32) NOT NULL DEFAULT 'stop',
  `risk_level` tinyint(3) unsigned NOT NULL DEFAULT '1',
  `rebalance_period` varchar(32) DEFAULT NULL,
  `monitor_updated_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_strategy_id` (`strategy_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## 后端架构

### 1. 实体类 (Entity)

#### StrategyWarning
- `id`: Long
- `strategyId`: Long
- `title`: String
- `description`: String
- `eventTime`: LocalDateTime
- `riskLevel`: String (high/medium/low)
- `resolved`: Boolean

#### StrategyHeatmap
- `id`: Long
- `strategyId`: Long
- `industry`: String
- `comparisonDimension`: String
- `deviationValue`: BigDecimal

#### StrategyProfitCurvePoint
- `id`: Long
- `strategyId`: Long
- `pointDate`: LocalDate
- `netValue`: BigDecimal

#### StrategyMonitorMetrics
- `id`: Long
- `strategyId`: Long
- `systemStatus`: String (running/stop/paused)
- `riskLevel`: Integer (1-5)
- `rebalancePeriod`: String (weekly/monthly/quarterly)
- `monitorUpdatedAt`: LocalDateTime
- `updatedAt`: LocalDateTime (兼容性方法，映射到 monitorUpdatedAt)

### 2. Mapper 接口和 XML

#### StrategyWarningMapper
- `findByStrategyId(Long strategyId)`: 查询指定策略的所有预警，按时间倒序排列

#### StrategyHeatmapMapper
- `findByStrategyId(Long strategyId)`: 查询指定策略的热力图数据，按行业和维度排序

#### StrategyProfitCurveMapper
- `findByStrategyId(Long strategyId)`: 查询指定策略的收益曲线数据，按日期升序排列

#### StrategyMonitorMetricsMapper
- `findByStrategyId(Long strategyId)`: 查询指定策略的监控指标（从 strategy_metrics 表）

### 3. Service 层 (StrategyMonitoringService)

```java
@Service
public class StrategyMonitoringService {
    // 获取监控指标（可能返回 null）
    StrategyMonitorMetrics getMetrics(Long strategyId)
    
    // 获取预警列表（返回空列表如果无数据）
    List<StrategyWarning> getWarnings(Long strategyId)
    
    // 获取收益曲线（返回空列表如果无数据）
    List<StrategyProfitCurvePoint> getProfitCurve(Long strategyId)
    
    // 获取热力图数据（返回空列表如果无数据）
    List<StrategyHeatmap> getHeatmap(Long strategyId)
}
```

### 4. Controller 层 (StrategyMonitoringController)

**基础路径**: `/api/strategy-monitoring`

#### POST /Metrics
- **请求体**: `{ "id": 1 }` (策略ID)
- **响应**: `StrategyMonitorMetrics` 对象或空 HashMap（如果不存在）
- **认证**: 需要有效的 Session

#### POST /Warnings
- **请求体**: `{ "id": 1 }` (策略ID)
- **响应**: `List<StrategyWarning>`（可能为空列表）
- **认证**: 需要有效的 Session

#### POST /ProfitCurve
- **请求体**: `{ "id": 1 }` (策略ID)
- **响应**: `List<StrategyProfitCurvePoint>`（可能为空列表）
- **认证**: 需要有效的 Session

#### POST /Heatmap
- **请求体**: `{ "id": 1 }` (策略ID)
- **响应**: `List<StrategyHeatmap>`（可能为空列表）
- **认证**: 需要有效的 Session

## 前端调用逻辑

### 数据加载流程

1. **选择策略**: 用户从下拉框选择策略
2. **并行请求**: 前端同时发起4个 API 请求
   ```javascript
   const [metricsResponse, warningsResponse, profitResponse, heatmapResponse] = 
     await Promise.all([
       http.post('/strategy-monitoring/Metrics', { id: strategyId }),
       http.post('/strategy-monitoring/Warnings', { id: strategyId }),
       http.post('/strategy-monitoring/ProfitCurve', { id: strategyId }),
       http.post('/strategy-monitoring/Heatmap', { id: strategyId })
     ])
   ```
3. **数据处理**:
   - **Metrics**: 提取 systemStatus, updatedAt, rebalancePeriod, riskLevel
   - **Warnings**: 映射为前端展示格式，包含 title, time, description, level, resolved
   - **ProfitCurve**: 用于绘制折线图（日期 vs 净值）
   - **Heatmap**: 用于绘制热力图（行业 vs 维度 vs 偏离度）
4. **图表渲染**: 使用 ECharts 渲染收益曲线和热力图

### 前端展示

1. **关键指标卡片**:
   - 运行状态（从 metrics.systemStatus）
   - 最近更新时间（从 metrics.updatedAt）
   - 再平衡周期（从 metrics.rebalancePeriod）
   - 风险等级（从 metrics.riskLevel）

2. **预警中心**: 列表展示预警信息，支持按风险等级过滤

3. **收益曲线图**: 折线图展示净值随时间变化

4. **热力图**: 展示不同行业在不同维度下的持仓偏离度

5. **风险指标图**: 饼图展示风险分布（目前使用硬编码数据）

## 数据流图

```
前端选择策略
    ↓
并行请求4个API
    ↓
┌─────────────────────────────────────┐
│  /Metrics    →  strategy_metrics    │
│  /Warnings   →  strategy_warning    │
│  /ProfitCurve →  strategy_profit_curve│
│  /Heatmap    →  strategy_heatmap     │
└─────────────────────────────────────┘
    ↓
Service层处理数据
    ↓
Controller返回JSON
    ↓
前端解析并渲染
    ↓
展示监控数据
```

## 注意事项

1. **数据一致性**: 
   - `strategy_metrics` 表应该为每个策略创建一条记录
   - 如果 metrics 不存在，Controller 返回空 HashMap，前端需要处理这种情况

2. **性能优化**:
   - 使用并行请求减少等待时间
   - 数据库表已建立索引优化查询性能

3. **错误处理**:
   - 所有 API 都需要认证，未登录返回 401
   - 策略ID为空返回 400
   - 数据不存在时返回空列表或空对象

4. **数据更新**:
   - 监控数据需要定期更新到对应的表中
   - `strategy_metrics.monitor_updated_at` 字段记录指标更新时间

