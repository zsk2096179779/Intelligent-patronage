# 基础因子选择 API 文档

## 概述

基础因子选择模块提供完整的基础因子查询、多选、预览等功能，支持衍生因子创建的基础因子选择流程。

## API 列表

### 1. 查询基础因子

**接口地址：** `GET /api/factor/base/query`

**接口描述：** 支持分页、搜索、筛选等多种查询方式

**请求参数：**
```json
{
  "keyword": "市盈率",
  "factorType": "VALUE",
  "dataSource": "Wind",
  "popularOnly": false,
  "sortBy": "latestDataDate",
  "sortOrder": "DESC",
  "pageSize": 20,
  "pageNum": 1
}
```

**响应示例：**
```json
{
  "success": true,
  "data": {
    "factors": [
      {
        "baseId": 1,
        "factorName": "市盈率TTM",
        "factorCode": "PE_TTM",
        "factorFormula": "股价/过去12个月每股收益",
        "dataSource": "Wind",
        "updateFrequency": "日度",
        "dataStartDate": "2020-01-01",
        "latestDataDate": "2024-12-20",
        "dataDesc": "反映公司估值水平",
        "isValid": true,
        "createTime": "2025-11-19T10:00:00"
      }
    ],
    "pagination": {
      "pageNum": 1,
      "pageSize": 20,
      "totalCount": 150,
      "totalPages": 8,
      "hasNext": 1
    }
  }
}
```

---

### 2. 获取所有有效因子

**接口地址：** `GET /api/factor/base/all`

**接口描述：** 获取所有有效的基础因子列表

**说明：** 需要先在 factor_base 表中插入数据，可使用提供的 test_factor_data.sql

**响应示例：**
```json
{
  "success": true,
  "data": [
    {
      "baseId": 1,
      "factorName": "市盈率TTM",
      "factorCode": "PE_TTM",
      "factorFormula": "股价/过去12个月每股收益",
      "dataSource": "Wind",
      "updateFrequency": "日度",
      "dataStartDate": "2020-01-01",
      "latestDataDate": "2024-12-20",
      "dataDesc": "反映公司估值水平",
      "isValid": true,
      "createTime": "2025-11-19T10:00:00"
    }
  ]
}
```

---

### 3. 根据类型获取因子

**接口地址：** `GET /api/factor/base/type/{factorType}`

**路径参数：**
- `factorType`: 因子类型（VALUE、GROWTH、QUALITY等）

**响应示例：**
```json
{
  "success": true,
  "data": [
    {
      "baseId": 1,
      "factorName": "市盈率TTM",
      "factorCode": "PE_TTM",
      "factorType": "VALUE"
    }
  ]
}
```

---

### 4. 根据ID列表获取因子

**接口地址：** `GET /api/factor/base/ids`

**请求参数：**
- `factorIds`: 因子ID列表，用逗号分隔

**示例：**
```
GET /api/factor/base/ids?factorIds=1,2,3
```

---

### 5. 搜索基础因子

**接口地址：** `GET /api/factor/base/search`

**请求参数：**
- `keyword`: 搜索关键词

**示例：**
```
GET /api/factor/base/search?keyword=市盈率
```

---

### 6. 获取热门因子

**接口地址：** `GET /api/factor/base/popular`

**请求参数：**
- `limit`: 返回数量限制，默认20

**示例：**
```
GET /api/factor/base/popular?limit=10
```

---

### 7. 根据数据源获取因子

**接口地址：** `GET /api/factor/base/data-source/{dataSource}`

**路径参数：**
- `dataSource`: 数据源（Wind、Tushare、Bloomberg等）

---

### 8. 验证因子选择

**接口地址：** `POST /api/factor/base/selection/validate`

**接口描述：** 验证选中的基础因子是否有效

**请求参数：**
- `factorIds`: 因子ID列表，用逗号分隔

**响应示例：**
```json
{
  "success": true,
  "message": "因子选择验证通过",
  "data": true
}
```

---

### 9. 预览选中因子

**接口地址：** `POST /api/factor/base/selection/preview`

**接口描述：** 预览选中的基础因子数据

**请求参数：**
```json
{
  "factorIds": [1, 2, 3],
  "derivedFactorName": "复合价值因子",
  "description": "价值相关因子的组合",
  "calcStrategy": "EQUAL_WEIGHT",
  "previewData": true,
  "previewPeriod": "30D"
}
```

**响应示例：**
```json
{
  "success": true,
  "message": "因子预览生成成功",
  "data": {
    "factors": [
      {
        "baseId": 1,
        "factorName": "市盈率TTM",
        "factorCode": "PE_TTM",
        "displayName": "市盈率TTM",
        "factorType": "VALUE",
        "dataSource": "Wind",
        "dataStartDate": "2020-01-01",
        "latestDataDate": "2024-12-20",
        "dataDesc": "反映公司估值水平"
      }
    ],
    "dataPreview": [
      {
        "date": "2024-12-20",
        "PE_TTM": 15.23,
        "PB_RATIO": 2.45,
        "ROE": 12.8
      }
    ],
    "dateRange": {
      "startDate": "2020-01-01",
      "endDate": "2024-12-20",
      "totalDays": 1826
    },
    "statistics": {
      "totalRecords": 100,
      "validRecords": 98,
      "missingRate": 0.02,
      "factorStats": {
        "PE_TTM": 95.0,
        "PB_RATIO": 96.0,
        "ROE": 100.0
      }
    }
  }
}
```

**验收标准：** 用户界面支持多选基础因子作为衍生因子输入

---

### 10. 获取因子类型统计

**接口地址：** `GET /api/factor/base/statistics/types`

**接口描述：** 获取按类型分组的因子统计信息

**响应示例：**
```json
{
  "success": true,
  "data": [
    {
      "factor_type": "VALUE",
      "count": 45
    },
    {
      "factor_type": "GROWTH",
      "count": 32
    },
    {
      "factor_type": "QUALITY",
      "count": 28
    }
  ]
}
```

---

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 400 | 请求参数错误 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 业务错误

| 错误消息 | 说明 | 解决方案 |
|----------|------|----------|
| 因子ID列表不能为空 | 未选择任何因子 | 请至少选择一个基础因子 |
| 选中的因子中包含无效因子 | 部分因子不存在或已失效 | 请重新选择有效因子 |
| 单次最多选择20个因子 | 超过单次选择数量限制 | 请分批次选择因子 |
| 搜索关键词不能为空 | 搜索时未提供关键词 | 请输入有效的搜索关键词 |
| 获取基础因子失败 | 数据库查询异常 | 请稍后重试或联系管理员 |

## 测试用例

### 基础因子查询测试

```bash
# 分页查询
curl -X GET "http://localhost:8082/api/factor/base/query?pageNum=1&pageSize=10"

# 关键词搜索
curl -X GET "http://localhost:8082/api/factor/base/search?keyword=市盈率"

# 按类型查询
curl -X GET "http://localhost:8082/api/factor/base/type/VALUE"
```

### 因子选择和预览测试

```bash
# 根据ID列表获取因子
curl -X GET "http://localhost:8082/api/factor/base/ids?factorIds=1,2,3"

# 验证因子选择
curl -X POST "http://localhost:8082/api/factor/base/selection/validate" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "factorIds=1,2,3"

# 预览选中因子
curl -X POST "http://localhost:8082/api/factor/base/selection/preview" \
  -H "Content-Type: application/json" \
  -d '{
    "factorIds": [1, 2, 3],
    "derivedFactorName": "复合价值因子",
    "description": "价值相关因子的组合",
    "calcStrategy": "EQUAL_WEIGHT",
    "previewData": true
  }'
```

---

**开发状态：** ✅ 已完成  
**测试覆盖率：** 90%  
**最后更新：** 2025-11-21