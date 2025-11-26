# 因子管理与检验完整流程指南

## 📋 概述

本指南涵盖从因子创建到因子检验的完整流程，包括：
1. **基础因子创建** - 通过SQL脚本创建基础因子
2. **衍生因子创建** - 通过API接口创建衍生因子
3. **因子检验** - 创建检验任务并获取IC/IR结果

---

## 🔧 第一部分：创建因子

### 1.1 创建基础因子

基础因子通过SQL脚本直接插入数据库。

#### 执行SQL脚本

**文件位置：** `src/main/resources/sql/create_sample_factors.sql`

**包含的基础因子：**

1. **基金收益率因子** (`FUND_RETURN`) - 直接使用change_rate
2. **基金波动率因子** (`FUND_VOLATILITY`) - 20日收益率标准差
3. **基金动量因子** (`FUND_MOMENTUM`) - 20日收益率
4. **基金最大回撤因子** (`FUND_MAX_DRAWDOWN`) - 历史最大回撤
5. **基金夏普比率因子** (`FUND_SHARPE`) - 收益率均值/标准差
6. **基金均值回归因子** (`FUND_MEAN_REVERSION`) - 净值与均值偏离度

**执行方式：**

```bash
# 方式1：命令行执行
mysql -u root -p your_database < src/main/resources/sql/create_sample_factors.sql

# 方式2：在数据库客户端中执行SQL文件
```

**SQL示例：**

```sql
-- 示例：创建收益率因子
INSERT INTO factor_base (
    factor_name, factor_code, factor_formula, data_source, 
    update_frequency, data_start_date, latest_data_date, 
    data_desc, is_valid
) VALUES (
    '基金收益率因子',
    'FUND_RETURN',
    '{"type":"formula","version":"1.0","expression":"change_rate","dataFields":["change_rate"],"description":"收益率因子：直接使用change_rate字段"}',
    'fund_etf_spot_ths',
    '日度',
    '2020-01-01',
    CURDATE(),
    '基金收益率因子，直接使用fund_etf_spot_ths表的change_rate字段',
    1
);
```

---

### 1.2 创建衍生因子

衍生因子通过API接口创建，基于基础因子进行加权组合。

#### 接口：`POST /api/factor/factor-management/derived-factors/basic`

#### 示例1：创建价值综合因子

**请求：**

```json
POST /api/factor/factor-management/derived-factors/basic
Content-Type: application/json

{
  "operationType": "CREATE_DERIVED_FACTOR",
  "derivedFactorInfo": {
    "factorName": "价值综合因子",
    "factorCode": "VAL_COM_FACTOR",
    "factorDesc": "收益率与波动率的加权组合，用于评估基金价值",
    "calcStrategyId": 1,
    "baseFactorIds": [1, 2],
    "styleTagCodes": "001"
  },
  "weightConfigInfo": {
    "baseFactorWeights": "{\"1\":0.6,\"2\":0.4}"
  }
}
```

**cURL 命令：**

```bash
curl -X POST http://localhost:8080/api/factor/factor-management/derived-factors/basic \
  -H "Content-Type: application/json" \
  -d '{
    "operationType": "CREATE_DERIVED_FACTOR",
    "derivedFactorInfo": {
      "factorName": "价值综合因子",
      "factorCode": "VAL_COM_FACTOR",
      "factorDesc": "收益率与波动率的加权组合",
      "calcStrategyId": 1,
      "baseFactorIds": [1, 2],
      "styleTagCodes": "001"
    },
    "weightConfigInfo": {
      "baseFactorWeights": "{\"1\":0.6,\"2\":0.4}"
    }
  }'
```

#### 示例2：创建成长质量因子（等权）

**请求：**

```json
{
  "operationType": "CREATE_DERIVED_FACTOR",
  "derivedFactorInfo": {
    "factorName": "成长质量因子",
    "factorCode": "GROWTH_QUALITY",
    "factorDesc": "动量与夏普比率的等权组合",
    "calcStrategyId": 2,
    "baseFactorIds": [3, 5],
    "styleTagCodes": "002"
  }
}
```

**说明：**
- `calcStrategyId = 2` 表示等权平均策略，无需配置权重
- 如果不提供 `weightConfigInfo`，系统会自动等权分配

#### 示例3：创建风险收益综合因子

**请求：**

```json
{
  "operationType": "CREATE_DERIVED_FACTOR",
  "derivedFactorInfo": {
    "factorName": "风险收益综合因子",
    "factorCode": "RISK_RETURN_COM",
    "factorDesc": "收益率、波动率、最大回撤的三因子组合",
    "calcStrategyId": 1,
    "baseFactorIds": [1, 2, 4],
    "styleTagCodes": "001,002"
  },
  "weightConfigInfo": {
    "baseFactorWeights": "{\"1\":0.5,\"2\":0.3,\"4\":0.2}"
  }
}
```

---

### 1.3 参数说明

#### derivedFactorInfo 参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| `factorName` | String | 是 | 因子名称 | "价值综合因子" |
| `factorCode` | String | 是 | 因子编码（唯一） | "VAL_COM_FACTOR" |
| `factorDesc` | String | 否 | 因子描述 | "收益率与波动率的组合" |
| `calcStrategyId` | Integer | 否 | 计算策略ID（默认1） | 1=加权组合, 2=等权平均 |
| `baseFactorIds` | List<Integer> | 是 | 基础因子ID列表 | [1, 2, 3] |
| `styleTagCodes` | String | 否 | 风格标签编码（逗号分隔） | "001,002" |

#### weightConfigInfo 参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| `baseFactorWeights` | String | 否 | 权重配置（JSON字符串） | `"{\"1\":0.6,\"2\":0.4}"` |

**权重配置格式：**
```json
{
  "基础因子ID": 权重值,
  "基础因子ID": 权重值
}
```

**注意：**
- 权重值范围：0-1
- 权重会自动归一化（总和为1）
- 如果使用等权策略（calcStrategyId=2），无需配置权重

#### 计算策略说明

| 策略ID | 策略名称 | 说明 |
|--------|---------|------|
| 1 | 归一化处理后的加权组合 | 对因子值标准化后加权求和 |
| 2 | 简单等权平均 | 所有因子权重相等 |
| 3 | 市值加权 | 按市值分配权重（基金场景不常用） |
| 4 | 风险调整加权 | 考虑风险调整的权重分配 |

---

### 1.4 验证因子创建成功

创建完成后，可以使用以下接口验证：

**查询基础因子列表：**
```bash
GET /api/factor/factor-management/base-factors
```

**查询衍生因子列表：**
```bash
GET /api/factor/factor-management/derived-factors
```

---

## 📊 第二部分：因子检验

### 2.1 创建检验任务

**接口：** `POST /api/factor/validation/tasks`

**描述：** 创建因子检验任务，系统会异步执行IC/IR计算

**请求参数：**

**方式1：数组格式（推荐）**
```json
{
  "taskName": "价值因子IC检验",
  "taskType": "IC_IR",
  "factorNames": ["基金收益率因子", "基金波动率因子", "基金动量因子"],
  "startDate": "2020-01-01",
  "endDate": "2025-11-20",
  "createUserId": 1
}
```

**方式2：字符串格式（也支持）**
```json
{
  "taskName": "价值因子IC检验",
  "taskType": "IC_IR",
  "factorNames": "[\"基金收益率因子\"]",
  "startDate": "2020-01-01",
  "endDate": "2025-11-20",
  "createUserId": 1
}
```

**参数说明：**

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| `taskName` | String | 否 | 任务名称，如果不提供会自动生成 | "价值因子IC检验" |
| `taskType` | String | 否 | 任务类型，默认为 "IC_IR" | "IC_IR" |
| `factorNames` | List<String> | **是** | 要检验的因子名称列表 | ["基金收益率因子", "基金波动率因子"] |
| `startDate` | String (Date) | **是** | 回测开始日期，格式：yyyy-MM-dd | "2020-01-01" |
| `endDate` | String (Date) | **是** | 回测结束日期，格式：yyyy-MM-dd | "2025-11-20" |
| `createUserId` | Integer | 否 | 创建人ID | 1 |

**注意：**
- `factorNames` 支持两种格式：
  - **数组格式**（推荐）：`["基金收益率因子", "基金波动率因子"]`
  - **字符串格式**：`"[\"基金收益率因子\"]"`（系统会自动解析）
- 使用 `factorNames` 时，系统会自动查找基础因子和衍生因子
- 支持同时检验基础因子和衍生因子
- 如果某个因子名称找不到，会在错误消息中提示

**cURL 示例：**

```bash
curl -X POST http://localhost:8080/api/factor/validation/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "taskName": "价值因子IC检验",
    "factorNames": ["基金收益率因子", "基金波动率因子"],
    "startDate": "2020-01-01",
    "endDate": "2025-11-20",
    "createUserId": 1
  }'
```

**响应示例：**

```json
{
  "code": 200,
  "message": "任务创建成功，正在后台执行计算",
  "data": {
    "operationType": "CREATE_VALIDATION_TASK",
    "success": true,
    "message": "任务创建成功，正在后台执行计算",
    "taskInfo": {
      "taskId": 1,
      "taskName": "价值因子IC检验",
      "taskType": "IC_IR",
      "taskStatus": "PENDING",
      "progress": 0,
      "createTime": "2025-11-22T10:00:00"
    },
    "operationTime": "2025-11-22T10:00:00"
  }
}
```

**重要：** 从响应中获取 `taskId`，用于后续查询。

---

### 2.2 查询任务状态

**接口：** `GET /api/factor/validation/tasks/{taskId}/status`

**描述：** 查询检验任务的执行状态和进度（需要轮询查询）

**请求参数：**

| 参数名 | 类型 | 位置 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| `taskId` | Integer | Path | **是** | 任务ID | 1 |

**cURL 示例：**

```bash
curl -X GET http://localhost:8080/api/factor/validation/tasks/1/status
```

**响应示例：**

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "operationType": "GET_TASK_STATUS",
    "success": true,
    "message": "查询成功",
    "taskInfo": {
      "taskId": 1,
      "taskName": "价值因子IC检验",
      "taskType": "IC_IR",
      "taskStatus": "RUNNING",
      "progress": 45,
      "createTime": "2025-11-22T10:00:00",
      "startTime": "2025-11-22T10:00:05",
      "endTime": null
    },
    "operationTime": "2025-11-22T10:01:00"
  }
}
```

**任务状态说明：**

- `PENDING` - 待执行
- `RUNNING` - 执行中
- `SUCCESS` - 成功
- `FAILED` - 失败

**建议：** 每隔几秒查询一次，直到 `taskStatus = "SUCCESS"` 或 `"FAILED"`

---

### 2.3 查询任务结果

**接口：** `GET /api/factor/validation/tasks/{taskId}/results`

**描述：** 查询检验任务的IC/IR计算结果（任务完成后调用）

**请求参数：**

| 参数名 | 类型 | 位置 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| `taskId` | Integer | Path | **是** | 任务ID | 1 |

**cURL 示例：**

```bash
curl -X GET http://localhost:8080/api/factor/validation/tasks/1/results
```

**响应示例：**

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "operationType": "GET_TASK_RESULTS",
    "success": true,
    "message": "查询成功",
    "taskInfo": {
      "taskId": 1,
      "taskName": "价值因子IC检验",
      "taskType": "IC_IR",
      "taskStatus": "SUCCESS",
      "progress": 100,
      "createTime": "2025-11-22T10:00:00",
      "startTime": "2025-11-22T10:00:05",
      "endTime": "2025-11-22T10:05:30"
    },
    "icIrResults": [
      {
        "resultId": 1,
        "factorId": 1,
        "factorCode": "VAL_COM_FACTOR",
        "factorName": "价值综合因子",
        "icMean": 0.123456,
        "icStd": 0.045678,
        "irValue": 2.701234,
        "icPositiveRatio": 0.65,
        "icSequence": {
          "2025-11-19": 0.123456,
          "2025-11-20": 0.234567,
          "2025-11-21": 0.145678
        },
        "calculationDate": "2025-11-20"
      },
      {
        "resultId": 2,
        "factorId": 2,
        "factorCode": "GROWTH_QUALITY",
        "factorName": "成长质量因子",
        "icMean": 0.098765,
        "icStd": 0.056789,
        "irValue": 1.738462,
        "icPositiveRatio": 0.58,
        "icSequence": {
          "2025-11-19": 0.098765,
          "2025-11-20": 0.187654,
          "2025-11-21": 0.123456
        },
        "calculationDate": "2025-11-20"
      }
    ],
    "operationTime": "2025-11-22T10:06:00"
  }
}
```

**结果字段说明：**

| 字段名 | 类型 | 说明 |
|--------|------|------|
| `icMean` | BigDecimal | IC均值（信息系数均值） |
| `icStd` | BigDecimal | IC标准差 |
| `irValue` | BigDecimal | IR值（信息比率 = IC均值 / IC标准差） |
| `icPositiveRatio` | BigDecimal | IC正相关比例（IC>0的天数/总天数） |
| `icSequence` | Map<Date, BigDecimal> | IC序列（日期 -> IC值），用于绘制走势图 |

**指标解读：**

- **IC均值**：|IC| > 0.05 表示因子有效，|IC| > 0.1 表示因子较强
- **IR值**：IR > 1 表示因子预测能力稳定，IR > 2 表示因子优秀
- **IC正相关比例**：> 0.5 表示因子整体有效

---

### 2.4 查询所有任务

**接口：** `GET /api/factor/validation/tasks`

**描述：** 查询所有检验任务列表

**请求参数：** 无

**cURL 示例：**

```bash
curl -X GET http://localhost:8080/api/factor/validation/tasks
```

**响应示例：**

```json
{
  "code": 200,
  "message": "查询成功，共3个任务",
  "data": {
    "operationType": "GET_ALL_TASKS",
    "success": true,
    "message": "查询成功，共3个任务",
    "operationTime": "2025-11-22T10:10:00"
  }
}
```

---

### 2.5 查询因子历史结果

**接口：** `GET /api/factor/validation/factors/{factorId}/history`

**描述：** 查询指定因子的历史IC/IR计算结果

**请求参数：**

| 参数名 | 类型 | 位置 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| `factorId` | Integer | Path | **是** | 因子ID | 1 |
| `limit` | Integer | Query | 否 | 返回条数限制，默认10 | 10 |

**cURL 示例：**

```bash
# 查询因子1的历史结果，返回最近10条
curl -X GET http://localhost:8080/api/factor/validation/factors/1/history

# 查询因子1的历史结果，返回最近20条
curl -X GET http://localhost:8080/api/factor/validation/factors/1/history?limit=20
```

**响应示例：**

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "operationType": "GET_FACTOR_HISTORY_RESULTS",
    "success": true,
    "message": "查询成功",
    "icIrResults": [
      {
        "resultId": 5,
        "factorId": 1,
        "factorCode": "VAL_COM_FACTOR",
        "factorName": "价值综合因子",
        "icMean": 0.123456,
        "icStd": 0.045678,
        "irValue": 2.701234,
        "icPositiveRatio": 0.65,
        "icSequence": {
          "2025-11-19": 0.123456,
          "2025-11-20": 0.234567
        },
        "calculationDate": "2025-11-20"
      }
    ],
    "operationTime": "2025-11-22T10:15:00"
  }
}
```

---

## 🔄 完整流程示例

### 步骤1：创建基础因子（执行SQL）

```bash
# 连接到数据库
mysql -u root -p your_database

# 执行SQL脚本
source src/main/resources/sql/create_sample_factors.sql
```

### 步骤2：创建衍生因子（使用API）

```bash
# 创建价值综合因子
curl -X POST http://localhost:8080/api/factor/factor-management/derived-factors/basic \
  -H "Content-Type: application/json" \
  -d '{
    "operationType": "CREATE_DERIVED_FACTOR",
    "derivedFactorInfo": {
      "factorName": "价值综合因子",
      "factorCode": "VAL_COM_FACTOR",
      "factorDesc": "收益率与波动率的加权组合",
      "calcStrategyId": 1,
      "baseFactorIds": [1, 2]
    },
    "weightConfigInfo": {
      "baseFactorWeights": "{\"1\":0.6,\"2\":0.4}"
    }
  }'
```

### 步骤3：创建检验任务

```bash
curl -X POST http://localhost:8080/api/factor/validation/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "taskName": "价值因子IC检验",
    "factorNames": ["基金收益率因子", "基金波动率因子"],
    "startDate": "2020-01-01",
    "endDate": "2025-11-20"
  }'
```

**从响应中获取 `taskId`，例如：`taskId = 1`**

### 步骤4：轮询查询任务状态

```bash
# 每隔几秒查询一次，直到 taskStatus = "SUCCESS" 或 "FAILED"
curl -X GET http://localhost:8080/api/factor/validation/tasks/1/status
```

### 步骤5：查询计算结果

```bash
# 当任务状态为 SUCCESS 时，查询结果
curl -X GET http://localhost:8080/api/factor/validation/tasks/1/results
```

### 步骤6：查看因子历史结果（可选）

```bash
# 查看因子1的历史检验结果
curl -X GET http://localhost:8080/api/factor/validation/factors/1/history?limit=10
```

---

## 📊 流程总览图

```
┌─────────────────────────────────────────────────────────────┐
│                   因子管理与检验完整流程                      │
└─────────────────────────────────────────────────────────────┘

【第一部分：创建因子】
    ↓
1. 创建基础因子（SQL脚本）
   └─ 执行 create_sample_factors.sql
   └─ 创建6个基础因子（收益率、波动率、动量等）
    ↓
2. 创建衍生因子（API接口）
   └─ POST /api/factor/factor-management/derived-factors/basic
   └─ 基于基础因子加权组合
    ↓
3. 验证因子创建成功
   └─ GET /api/factor/factor-management/derived-factors
    ↓
【第二部分：因子检验】
    ↓
4. 创建检验任务
   └─ POST /api/factor/validation/tasks
   └─ 返回 taskId
    ↓
5. 轮询查询任务状态
   └─ GET /api/factor/validation/tasks/{taskId}/status
   └─ 直到 taskStatus = "SUCCESS"
    ↓
6. 查询计算结果
   └─ GET /api/factor/validation/tasks/{taskId}/results
   └─ 获取 IC/IR 数据
    ↓
7. [可选] 查看历史结果
   └─ GET /api/factor/validation/factors/{factorId}/history
```

---

## ⚠️ 注意事项

### 因子创建注意事项

1. **因子编码唯一性**：`factor_code` 必须唯一，不能重复
2. **基础因子ID**：创建衍生因子时，`baseFactorIds` 中的ID必须在 `factor_base` 表中存在
3. **权重归一化**：系统会自动将权重归一化，确保总和为1
4. **风格标签**：`styleTagCodes` 需要在 `style_tag` 表中存在
5. **公式格式**：基础因子的 `factor_formula` 必须是有效的JSON格式

### 因子检验注意事项

1. **异步执行**：创建任务后，计算在后台异步执行，需要轮询查询状态
2. **任务ID**：创建任务后返回的 `taskId` 用于后续查询
3. **日期格式**：日期参数使用 `yyyy-MM-dd` 格式
4. **因子名称**：必须使用因子名称创建任务，支持基础因子和衍生因子
5. **因子名称查询**：系统会先查找衍生因子，再查找基础因子
6. **数据完整性**：确保 `fund_etf_spot_ths` 表有对应日期范围的数据
6. **计算时间**：根据数据量和因子数量，计算可能需要几分钟到几十分钟

---

## 📈 计算原理说明

### 因子值计算

1. **基础因子**：
   - 从 `factor_base.factor_formula` 读取JSON格式公式
   - 结合 `fund_etf_spot_ths` 表的当日基金数据
   - 根据表达式计算因子值

2. **衍生因子**：
   - 先计算关联的基础因子值
   - 根据权重加权求和：`衍生因子值 = Σ(基础因子值_i × 权重_i)`

### IC/IR计算

1. **IC值（Information Coefficient）**：
   - 对每个交易日，计算所有基金的因子值与下期收益率的相关系数
   - `IC = corr(因子值, 下期收益率)`

2. **IC统计量**：
   - `IC均值 = Σ(IC值) / n`
   - `IC标准差 = √(Σ(IC值 - IC均值)² / n)`
   - `IR值 = IC均值 / IC标准差`

3. **IC正相关比例**：
   - `IC正相关比例 = IC > 0 的天数 / 总天数`

---

## 🎯 快速参考

### 常用接口

| 操作 | 接口 | 方法 |
|------|------|------|
| 创建衍生因子 | `/api/factor/factor-management/derived-factors/basic` | POST |
| 查询衍生因子 | `/api/factor/factor-management/derived-factors` | GET |
| 创建检验任务 | `/api/factor/validation/tasks` | POST |
| 查询任务状态 | `/api/factor/validation/tasks/{taskId}/status` | GET |
| 查询任务结果 | `/api/factor/validation/tasks/{taskId}/results` | GET |
| 查询历史结果 | `/api/factor/validation/factors/{factorId}/history` | GET |

### 关键字段

- **因子ID**：创建衍生因子后返回的 `derivedId`，用于检验
- **任务ID**：创建检验任务后返回的 `taskId`，用于查询状态和结果
- **IC序列**：用于绘制IC走势图，展示因子预测能力的时间变化

---

*文档更新日期：2025-11-22*

