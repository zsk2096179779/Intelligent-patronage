# 分层回测和持有期统计API使用说明

## 概述

分层回测功能提供完整的因子有效性分析，包括分位数累计收益率曲线、年化收益率对比和持有期收益分布统计。

## 核心功能

1. **分层回测计算**: 将股票按因子值分为5个分位组，计算各组的收益指标
2. **净值序列管理**: 存储每日净值数据，用于绘制累计收益率曲线
3. **持有期统计**: 计算不同持有期的收益分布统计（5天、10天、20天、60天、120天）

## API接口

### 1. 创建分层回测任务

**接口地址**: `POST /factor/validation/layered/tasks`

**请求参数**:
```json
{
  "taskName": "价值因子分层回测",
  "factorIds": [1, 2, 3],
  "factorNames": ["市盈率因子", "市净率因子", "动量因子"],
  "startDate": "2023-01-01",
  "endDate": "2024-01-01",
  "quantileCount": 5,
  "rebalanceFrequency": "MONTHLY",
  "equalWeighted": true,
  "fundCode": "159001",
  "benchmarkCode": "000300",
  "minStockCount": 30,
  "maxStockCount": 100,
  "excludeStStocks": true,
  "excludeSuspendedStocks": true,
  "excludeNewStocks": true,
  "remarks": "测试分层回测"
}
```

**响应结果**:
```json
{
  "success": true,
  "message": "分层回测任务创建并执行成功",
  "data": "任务ID: 200"
}
```

### 2. 获取分位数累计收益率曲线

**接口地址**: `GET /factor/validation/tasks/{taskId}/layered/cumulative-return`

**响应结果**:
```json
{
  "success": true,
  "message": "查询成功",
  "data": {
    "operationType": "GET_CUMULATIVE_RETURN_CURVE",
    "taskInfo": {
      "taskId": 200,
      "taskName": "价值因子分层回测",
      "taskType": "LAYERED",
      "taskStatus": "SUCCESS"
    },
    "cumulativeReturnCurve": [
      {
        "quantile": 1,
        "totalReturn": 0.05,
        "annualizedReturn": 0.048,
        "sharpeRatio": 0.4,
        "maxDrawdown": 0.08,
        "returnSeries": {
          "2023-01-01": 0.0000,
          "2023-01-02": 0.0025,
          "2023-01-03": 0.0080
        }
      }
    ]
  }
}
```

### 3. 获取分位数平均年化收益率

**接口地址**: `GET /factor/validation/tasks/{taskId}/layered/annualized-return`

**响应结果**:
```json
{
  "success": true,
  "message": "查询成功",
  "data": {
    "annualizedReturnChart": [
      {
        "quantile": 1,
        "quantileName": "Q1-低分位组",
        "avgAnnualizedReturn": 0.048,
        "annualizedReturnStd": 0.12,
        "positiveReturnRatio": 0.52,
        "winRate": 0.52
      },
      {
        "quantile": 5,
        "quantileName": "Q5-高分位组",
        "avgAnnualizedReturn": 0.172,
        "annualizedReturnStd": 0.145,
        "positiveReturnRatio": 0.83,
        "winRate": 0.83
      }
    ]
  }
}
```

### 4. 获取持有期收益分布

**接口地址**: `GET /factor/validation/tasks/{taskId}/layered/return-distribution?holdingPeriods=5,10,20,60,120`

**响应结果**:
```json
{
  "success": true,
  "message": "查询成功",
  "data": {
    "returnDistributionChart": [
      {
        "quantile": 5,
        "quantileName": "Q5-高分位组",
        "holdingPeriod": 20,
        "minReturn": -0.06,
        "q1Return": 0.028,
        "medianReturn": 0.014,
        "q3Return": 0.068,
        "maxReturn": 0.15,
        "meanReturn": 0.014,
        "stdReturn": 0.03,
        "winRate": 0.72,
        "outliers": []
      }
    ]
  }
}
```

### 5. 获取完整的分层回测数据

**接口地址**: `GET /factor/validation/tasks/{taskId}/layered/complete`

**响应结果**: 包含上述所有图表的完整数据

### 6. 执行分层回测计算

**接口地址**: `POST /factor/validation/tasks/{taskId}/layered/execute`

**响应结果**:
```json
{
  "success": true,
  "message": "分层回测执行成功",
  "data": "分层回测计算已完成"
}
```

## 数据库表结构

### 1. 分层回测结果表 (factor_layered_result)

存储每个分位组的回测统计指标：

- `result_id`: 结果ID（主键）
- `task_id`: 关联任务ID
- `factor_id`: 因子ID
- `quantile`: 分位数（1-5）
- `quantile_name`: 分位数名称（Q1-Q5）
- `total_return`: 总收益率
- `annualized_return`: 年化收益率
- `sharpe_ratio`: 夏普比率
- `max_drawdown`: 最大回撤
- `win_rate`: 胜率

### 2. 净值序列表 (factor_layered_nav_series)

存储每日净值和收益数据：

- `series_id`: 序列ID（主键）
- `result_id`: 关联结果ID
- `trade_date`: 交易日期
- `nav_value`: 净值值
- `daily_return`: 日收益率
- `cumulative_return`: 累计收益率

### 3. 持有期统计表 (factor_layered_holding_stats)

存储不同持有期的收益分布统计：

- `stats_id`: 统计ID（主键）
- `result_id`: 关联结果ID
- `holding_period`: 持有期（天）
- `mean_return`: 平均收益率
- `std_return`: 收益率标准差
- `min_return`: 最小收益率
- `q1_return`: 第一四分位数
- `median_return`: 中位数
- `q3_return`: 第三四分位数
- `max_return`: 最大收益率
- `win_rate`: 胜率

## 使用流程

1. **创建任务**: 调用创建分层回测任务接口
2. **自动计算**: 系统自动执行分层回测计算
3. **查询结果**: 使用各种查询接口获取分析结果
4. **数据可视化**: 前端根据返回数据绘制图表

## 注意事项

1. **数据完整性**: 确保因子数据和股票价格数据完整性
2. **计算性能**: 大规模回测可能需要较长时间，建议异步处理
3. **结果缓存**: 计算结果会被缓存，相同参数的查询会直接返回缓存结果
4. **权限控制**: 只有具备相应权限的用户才能创建和执行回测任务

## 错误处理

常见错误码及处理方式：

- `400`: 请求参数错误，检查参数格式和必填项
- `404`: 任务不存在，检查taskId是否正确
- `500`: 服务器内部错误，查看日志获取详细错误信息

## 示例代码

### JavaScript/TypeScript

```javascript
// 创建分层回测任务
const createTask = async () => {
  const response = await fetch('/factor/validation/layered/tasks', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      taskName: '价值因子分层回测',
      factorIds: [1, 2, 3],
      startDate: '2023-01-01',
      endDate: '2024-01-01'
    })
  });
  
  const result = await response.json();
  console.log('任务创建结果:', result);
};

// 获取累计收益率曲线
const getCumulativeReturn = async (taskId) => {
  const response = await fetch(`/factor/validation/tasks/${taskId}/layered/cumulative-return`);
  const result = await response.json();
  return result.data.cumulativeReturnCurve;
};
```

### Python

```python
import requests

def create_layered_backtest_task():
    url = "http://localhost:8080/factor/validation/layered/tasks"
    data = {
        "taskName": "价值因子分层回测",
        "factorIds": [1, 2, 3],
        "startDate": "2023-01-01",
        "endDate": "2024-01-01"
    }
    
    response = requests.post(url, json=data)
    return response.json()

def get_cumulative_return(task_id):
    url = f"http://localhost:8080/factor/validation/tasks/{task_id}/layered/cumulative-return"
    response = requests.get(url)
    return response.json()
```