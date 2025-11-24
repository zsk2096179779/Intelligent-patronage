# API接口说明文档

## 接口概述

本项目实现了完整的组合产品管理功能，包括策略管理、组合创建的多步骤配置（基础信息、产品参数、基金持仓）、查询和审核管理。所有接口采用统一的响应格式，并集成了跨域配置以支持前端调用。

## 已实现的接口

### 1. 创建组合产品

**接口地址：** `POST /api/strategy-combination`

**接口描述：** 创建新的策略组合产品，设置组合的基本信息（名称、风险等级、策略类型等），并关联策略引用ID（`strategies.strategy_ref_id`）。创建后的组合初始状态为未审核（`listed = 0`），需要经过审核流程才能上架。 

**请求方式：** POST

**请求体：**

```json
{
  "name": "稳健理财组合",
  "riskLevel": "中低风险",
  "strategyType": "大类资产配置",
  "strategyRefId": 1001
}
```

**请求体字段说明：**
- `name` (String, 必填): 组合名称
- `riskLevel` (String, 可选): 风险等级（如：低风险、中低风险、中风险、中高风险、高风险）
- `strategyType` (String, 可选): 策略类型（如：大类资产配置、FOF组合、基金指数组合、择时组合）
- `strategyRefId` (Integer, 必填): 策略引用ID（关联 `strategies.strategy_ref_id`）
- `summary` (String, 可选): 组合简介
- `targetInvestor` (String, 可选): 目标客户

**响应格式（成功）：**

```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "portfolioId": 1
  }
}
```

**响应格式（失败 - 参数错误）：**

```json
{
  "code": 400,
  "message": "创建失败：组合名称不能为空",
  "data": null
}
```

或

```json
{
  "code": 400,
  "message": "创建失败：策略引用ID不能为空",
  "data": null
}
```

**响应格式（服务器错误）：**

```json
{
  "code": 500,
  "message": "创建失败：错误信息",
  "data": null
}
```

**业务逻辑：**
- 创建组合时，`listed` 字段自动设置为 `0`（未审核状态）
- `status` 字段自动设置为 `draft`（草稿状态）
- `reject_reason` 字段为 `NULL`
- 创建成功后返回生成的组合ID
- 创建后的组合需要完成多步骤配置（基础信息、产品参数、持仓），然后提交审核，审核通过后才能上架

**SQL 实现：**
```sql
INSERT INTO portfolios (name, risk_level, strategy_type, strategy_id, listed)
VALUES (#{name}, #{riskLevel}, #{strategyType}, #{strategyRefId}, 0)
```

**请求示例：**

```bash
curl -X POST http://localhost:8080/api/strategy-combination \
  -H "Content-Type: application/json" \
  -d '{
    "name": "稳健理财组合",
    "riskLevel": "中低风险",
    "strategyType": "大类资产配置",
    "strategyRefId": 1001
  }'
```

### 2. 获取策略列表

**接口地址：** `GET /api/strategies`

**接口描述：** 查询所有策略列表，用于前端下拉框选择。返回策略的基本信息（ID、名称、类型、描述），供创建组合产品时选择关联的策略。

**请求方式：** GET

**请求参数：** 无

**响应格式：**

```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "strategyId": 1,
      "strategyName": "成长策略",
      "strategyType": "成长型",
      "description": "策略描述,可选",
      "strategyRefId": 1001
    },
    {
      "strategyId": 2,
      "strategyName": "稳健策略",
      "strategyType": "稳健型",
      "description": "稳健型投资策略",
      "strategyRefId": 1002
    }
  ]
}
```

**响应字段说明：**
- `strategyId` (Integer): 策略ID（唯一标识，用于创建组合时关联）
- `strategyName` (String): 策略名称
- `strategyType` (String): 策略类型（可选，用于展示）
- `description` (String): 策略描述（可选）
- `strategyRefId` (Integer): 策略引用ID（用于组合关联，应传给创建组合接口的 `strategyRefId` 字段）

**错误响应：**

当查询失败时，返回以下格式：

```json
{
  "code": 500,
  "message": "查询失败：错误信息",
  "data": null
}
```

**业务逻辑：**
- 查询 `strategies` 表中的所有策略
- 返回策略的基本信息，用于前端下拉框展示
- 按策略ID降序排列，最新的策略排在前面
- 前端需要将返回结果中的 `strategyRefId` 作为创建组合产品接口的 `strategyRefId` 参数

**SQL 实现：**
```sql
SELECT
    strategyid AS strategy_id,
    name AS strategy_name,
    strategy_type,
    description
FROM strategies
ORDER BY strategyid DESC
```

**使用场景：**
- 创建组合产品时，前端下拉框需要选择关联的策略
- 策略列表接口提供所有可用的策略供用户选择

**请求示例：**

```bash
curl http://localhost:8080/api/strategies
```

### 3. 获取所有策略组合

**接口地址：** `GET /combos`

**接口描述：** 查询所有策略组合及其关联的策略详细信息，通过 LEFT JOIN 关联 portfolios 表和 strategies 表，返回组合和策略的完整数据。

**请求方式：** GET

**请求参数：** 无

**响应格式：**

```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "portfolioId": 1,
      "portfolioName": "组合名称",
      "riskLevel": "风险等级",
      "portfolioStrategyType": "组合策略类型",
      "listed": 1,
      "status": "approved",
      "rejectReason": null,
      "strategyId": 1,
      "strategyName": "策略名称",
      "strategyType": "策略类型",
      "description": "策略描述",
      "strategyRefId": 1,
      "createTime": "2024-01-01T00:00:00",
      "scale": 100.0,
      "funds": "成份基金信息（JSON或列表）",
      "feeRate": 0.5,
      "returnRate": 15.5,
      "annualReturn": 12.3,
      "volatility": 8.5,
      "sharpeRatio": 1.2,
      "maxDrawdown": -5.2,
      "winRate": 65.5
    }
  ]
}
```

**响应字段说明：**

#### 组合信息字段
- `portfolioId` (Integer): 组合ID（主键）
- `portfolioName` (String): 组合名称
- `riskLevel` (String): 风险等级
- `portfolioStrategyType` (String): 组合策略类型
- `listed` (Integer): 是否上架（0-未上架/未审核，1-已上架/已审核，-1-已拒绝）
- `status` (String): 审核状态（`draft`/`pending_review`/`approved`/`rejected`）
- `rejectReason` (String): 审核拒绝原因（当 listed = -1 时可能有值）

#### 策略信息字段
- `strategyId` (Integer): 策略ID
- `strategyName` (String): 策略名称
- `strategyType` (String): 策略类型
- `description` (String): 策略描述
- `strategyRefId` (Integer): 对应策略的ID（外键）
- `createTime` (LocalDateTime): 创立时间
- `scale` (Double): 资产规模（亿元）
- `funds` (String): 成份基金（JSON或列表格式）
- `feeRate` (Double): 费率（%）
- `returnRate` (Double): 策略收益（%）
- `annualReturn` (Double): 策略年化收益（%）
- `volatility` (Double): 波动率
- `sharpeRatio` (Double): 夏普比率
- `maxDrawdown` (Double): 最大回撤（%）
- `winRate` (Double): 平仓胜率（%）

**错误响应：**

当查询失败时，返回以下格式：

```json
{
  "code": 500,
  "message": "查询失败：错误信息",
  "data": null
}
```

## 技术实现

### 数据查询逻辑

接口通过 MyBatis 执行 SQL 查询，使用 LEFT JOIN 关联 `portfolios` 表和 `strategies` 表：
- 主表：`portfolios` (p)
- 关联表：`strategies` (s)
- 关联条件：`p.strategy_id = s.strategy_ref_id`
- 排序：按组合ID降序排列

### 跨域配置

项目已配置 CORS 跨域支持，允许以下前端地址访问：
- `http://localhost:5173`
- `http://localhost:3000`
- `http://127.0.0.1:5173`

支持的 HTTP 方法：GET, POST, PUT, DELETE, OPTIONS, PATCH

## 数据返回说明

1. **统一响应格式**：所有接口返回统一格式，包含 `code`（状态码）、`message`（消息）和 `data`（数据）三个字段。

2. **数据完整性**：返回的 `PortfolioDetailDTO` 对象同时包含组合信息和策略信息，前端可以直接使用，无需额外关联查询。

3. **数据排序**：返回的组合列表按组合ID降序排列，最新的组合排在前面。

4. **空值处理**：如果某个组合没有关联的策略信息，策略相关字段可能为 null（由于使用 LEFT JOIN）。

### 4. 审核通过接口

**接口地址：** `POST /api/strategy-combination/{id}/approve`

**接口描述：** 审核通过指定的策略组合，将 `portfolios` 表中对应记录的 `listed` 字段从 `0` 更新为 `1`。只有当前状态为 `0`（未审核）的组合才能被审核通过。

**请求方式：** POST

**路径参数：**
- `id` (Integer): 策略组合ID

**请求体：** 无

**响应格式（成功）：**

```json
{
  "code": 200,
  "message": "审核通过成功",
  "data": null
}
```

**响应格式（失败 - 组合不存在或已审核）：**

```json
{
  "code": 400,
  "message": "审核失败：组合不存在或状态不正确（可能已经审核过）",
  "data": null
}
```

**响应格式（服务器错误）：**

```json
{
  "code": 500,
  "message": "审核失败：错误信息",
  "data": null
}
```

**业务逻辑：**
- 只有当 `listed = 0` 时才能审核通过
- 审核通过后 `listed` 更新为 `1`，同时清空之前的拒绝原因（`reject_reason = NULL`）
- 如果组合不存在或状态不是 `0`，返回 400 错误

**SQL 实现：**
```sql
UPDATE portfolios
SET listed = 1,
    reject_reason = NULL
WHERE id = #{id} AND listed = 0
```

### 5. 审核拒绝接口

**接口地址：** `POST /api/strategy-combination/{id}/reject`

**接口描述：** 审核拒绝指定的策略组合，将 `portfolios` 表中对应记录的 `listed` 字段更新为 `-1`，表示已拒绝状态。

**请求方式：** POST

**路径参数：**
- `id` (Integer): 策略组合ID

**请求体：**

```json
{
  "reason": "拒绝原因"
}
```

**请求体字段说明：**
- `reason` (String): 拒绝原因（会存储到数据库的 `reject_reason` 字段中）

**响应格式（成功）：**

```json
{
  "code": 200,
  "message": "审核拒绝成功",
  "data": null
}
```

**响应格式（失败 - 组合不存在）：**

```json
{
  "code": 400,
  "message": "审核失败：组合不存在",
  "data": null
}
```

**响应格式（服务器错误）：**

```json
{
  "code": 500,
  "message": "审核失败：错误信息",
  "data": null
}
```

**业务逻辑：**
- 审核拒绝后 `listed` 更新为 `-1`
- 拒绝原因会存储到数据库的 `reject_reason` 字段中
- 拒绝原因会在查询接口中返回，前端可以显示给用户

**SQL 实现：**
```sql
UPDATE portfolios
SET listed = -1,
    reject_reason = #{reason}
WHERE id = #{id}
```

**请求示例：**

```bash
curl -X POST http://localhost:8080/api/strategy-combination/1/reject \
  -H "Content-Type: application/json" \
  -d '{"reason": "不符合审核标准"}'
```

### 6. 更新组合基础信息

**接口地址：** `PUT /api/strategy-combination/{id}/basic-info`

**接口描述：** 更新组合的基础信息（名称、风险等级、策略类型、简介、目标客户等）。

**请求方式：** PUT

**路径参数：**
- `id` (Integer): 组合ID

**请求体：**

```json
{
  "name": "稳健理财组合",
  "riskLevel": "中低风险",
  "strategyType": "大类资产配置",
  "summary": "组合简介",
  "targetInvestor": "目标客户",
  "strategyId": 1001
}
```

**响应格式：** 统一响应格式

### 7. 保存产品参数

**接口地址：** `POST /api/strategy-combination/{id}/product-params`

**接口描述：** 保存或更新组合的产品参数（费率、投资金额限制、开放规则等）。

**请求方式：** POST

**路径参数：**
- `id` (Integer): 组合ID

**请求体：**

```json
{
  "minInvestAmount": 1000.00,
  "maxInvestAmount": 1000000.00,
  "subscriptionFee": 0.5,
  "redemptionFee": 0.1,
  "managementFee": 1.2,
  "openDayRule": "工作日开放",
  "redemptionRule": "T+1赎回"
}
```

**响应格式：** 统一响应格式

### 8. 查询产品参数

**接口地址：** `GET /api/strategy-combination/{id}/product-params`

**接口描述：** 查询组合的产品参数。

**请求方式：** GET

**路径参数：**
- `id` (Integer): 组合ID

**响应格式：**

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "minInvestAmount": 1000.00,
    "maxInvestAmount": 1000000.00,
    "subscriptionFee": 0.5,
    "redemptionFee": 0.1,
    "managementFee": 1.2,
    "openDayRule": "工作日开放",
    "redemptionRule": "T+1赎回"
  }
}
```

### 9. 保存持仓

**接口地址：** `POST /api/strategy-combination/{id}/holdings`

**接口描述：** 保存组合的基金持仓信息。会先删除旧的持仓，再插入新的持仓。持仓权重总和必须为100%（或 1.0）。后台会自动兼容前端传入的权重格式：如果传入 0-100 的百分比，会自动除以 100 存储；如果传入 0-1 的小数，会原样存储。**基金代码必须在 `funds` 表中存在，否则会返回错误。**

**请求方式：** POST

**路径参数：**
- `id` (Integer): 组合ID

**请求体：**

```json
[
  {
    "fundCode": "000001",
    "fundName": "基金名称1",
    "weight": 30.00,
    "remark": "备注"
  },
  {
    "fundCode": "000002",
    "fundName": "基金名称2",
    "weight": 70.00,
    "remark": "备注"
  }
]
```

**请求体字段说明：**
- `fundCode` (String, 必填): 基金代码（必须在 `funds` 表中存在）
- `fundName` (String, 可选): 基金名称（如果为空，会自动从 `funds` 表获取）
- `weight` (BigDecimal, 必填): 权重，支持两种格式：
  - `0-100` 的百分比（如 `30`、`70`）
  - `0-1` 的小数（如 `0.3`、`0.7`）
  后端会统一换算为小数存储，并在查询时返回百分比（`0-100`）。所有持仓权重总和必须为100%（或 1.0）。
- `remark` (String, 可选): 备注

**响应格式（成功）：**

```json
{
  "code": 200,
  "message": "保存成功",
  "data": null
}
```

**响应格式（失败 - 权重总和不为100%）：**

```json
{
  "code": 400,
  "message": "保存失败：持仓权重总和必须为100%（或 签约业务模块设计.md.0）",
  "data": null
}
```

**响应格式（失败 - 基金代码不存在）：**

```json
{
  "code": 400,
  "message": "保存失败：基金代码不存在：000001",
  "data": null
}
```

### 10. 查询持仓

**接口地址：** `GET /api/strategy-combination/{id}/holdings`

**接口描述：** 查询组合的持仓列表。

**请求方式：** GET

**路径参数：**
- `id` (Integer): 组合ID

**响应格式：**（返回值中的 `weight` 始终为百分比格式，即 `0-100`）

```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "id": 1,
      "fundCode": "000001",
      "fundName": "基金名称1",
      "weight": 30.00,
      "remark": "备注"
    },
    {
      "id": 2,
      "fundCode": "000002",
      "fundName": "基金名称2",
      "weight": 70.00,
      "remark": "备注"
    }
  ]
}
```

### 11. 提交审核

**接口地址：** `POST /api/strategy-combination/{id}/submit`

**接口描述：** 将草稿状态的组合提交审核，状态从 `draft` 更新为 `pending_review`。

**请求方式：** POST

**路径参数：**
- `id` (Integer): 组合ID

**响应格式（成功）：**

```json
{
  "code": 200,
  "message": "提交审核成功",
  "data": null
}
```

**响应格式（失败）：**

```json
{
  "code": 400,
  "message": "提交失败：组合不存在或状态不正确（只能提交草稿状态的组合）",
  "data": null
}
```

### 12. 查询基金列表

**接口地址：** `GET /api/funds`

**接口描述：** 获取所有基金列表，用于前端下拉框选择或搜索基金。支持关键词搜索。

**请求方式：** GET

**查询参数：**
- `keyword` (String, 可选): 搜索关键词（基金代码或名称模糊匹配）
- `fundType` (String, 可选): 基金类型（如：股票型、债券型等）
- `category` (String, 可选): 分类（如：权益类、货币类等）
- `operationCycle` (String, 可选): 运作周期
- `minFundSize` / `maxFundSize` (Double, 可选): 基金规模范围（单位：亿元）
- `minFeeRate` / `maxFeeRate` (Double, 可选): 费率范围（单位：%）

**响应格式（成功）：**

```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "fundCode": "000001",
      "fundName": "基金名称1",
      "fundDescription": "基金描述",
      "fundType": "股票型",
      "category": "权益类",
      "fundSize": 100.5,
      "feeRate": 1.5,
      "inceptionDate": "2020-01-01"
    }
  ]
}
```

**请求示例：**

```bash
# 查询所有基金
curl http://localhost:8080/api/funds

# 根据关键词搜索基金
curl "http://localhost:8080/api/funds?keyword=000001"

# 组合筛选示例：查询股票型、规模大于10亿元且费率小于1%的基金
curl "http://localhost:8080/api/funds?fundType=%E8%82%A1%E7%A5%A8%E5%9E%8B&minFundSize=10&maxFeeRate=1"
```

### 13. 根据基金代码查询基金信息

**接口地址：** `GET /api/funds/{fundCode}`

**接口描述：** 根据基金代码查询单个基金的详细信息。

**请求方式：** GET

**路径参数：**
- `fundCode` (String): 基金代码

**响应格式（成功）：**

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "fundCode": "000001",
    "fundName": "基金名称1",
    "fundDescription": "基金描述",
    "fundType": "股票型",
    "category": "权益类",
    "fundSize": 100.5,
    "feeRate": 1.5,
    "inceptionDate": "2020-01-01"
  }
}
```

**响应格式（失败 - 基金不存在）：**

```json
{
  "code": 404,
  "message": "基金不存在",
  "data": null
}
```

## 组合创建完整流程

组合创建的完整流程包括以下步骤：

1. **查询基金列表**（可选）→ `GET /api/funds` 或 `GET /api/funds?keyword=xxx`（用于选择基金）
2. **创建组合** → `POST /api/strategy-combination`（创建基础信息，状态为 `draft`）
3. **更新基础信息**（可选）→ `PUT /api/strategy-combination/{id}/basic-info`
4. **配置产品参数** → `POST /api/strategy-combination/{id}/product-params`
5. **配置基金持仓** → `POST /api/strategy-combination/{id}/holdings`（权重总和必须为100%，基金代码必须在 `funds` 表中存在）
6. **提交审核** → `POST /api/strategy-combination/{id}/submit`（状态变为 `pending_review`）
7. **审核通过/拒绝** → `POST /api/strategy-combination/{id}/approve` 或 `/reject`

## 数据库关联说明

### portfolio_holdings 表与 funds 表的关联

`portfolio_holdings` 表通过 `fund_code` 字段关联到 `funds` 表的 `fund_code`（主键）：

- **外键约束**：`fund_code` 必须在 `funds` 表中存在
- **数据完整性**：保存持仓时会自动验证基金代码是否存在
- **基金名称**：如果前端未传 `fundName`，系统会自动从 `funds` 表获取
- **查询优化**：查询持仓时会 LEFT JOIN `funds` 表，确保基金名称的准确性

**SQL 外键约束：**
```sql
ALTER TABLE portfolio_holdings
    ADD CONSTRAINT fk_holdings_fund 
    FOREIGN KEY (fund_code) REFERENCES funds(fund_code) 
    ON DELETE RESTRICT 
    ON UPDATE CASCADE;
```

## 审核状态说明

### `listed` 字段的状态值：
- `0`: 未上架/未审核
- `1`: 已上架/已审核通过
- `-1`: 已拒绝

### `status` 字段的状态值：
- `draft`: 草稿（可编辑）
- `pending_review`: 待审核（已提交，等待审核）
- `approved`: 已通过（审核通过）
- `rejected`: 已拒绝（审核拒绝）

**状态流转：**
```
创建 → draft → 提交审核 → pending_review → 审核通过 → approved
                                              ↓
                                          审核拒绝 → rejected
```

**拒绝原因存储：**
- 当审核拒绝时，拒绝原因会存储到 `reject_reason` 字段
- 当审核通过时，会清空之前的拒绝原因（`reject_reason = NULL`）
- 查询接口会返回 `rejectReason` 字段，前端可以根据 `listed = -1` 和 `rejectReason` 显示拒绝信息

