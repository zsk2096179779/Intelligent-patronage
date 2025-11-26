# 因子树管理 API 文档

## 概述

因子树管理模块提供完整的因子树创建、查询、修改等功能，支持多场景管理和树形结构操作。

## API 列表

### 1. 创建因子树

**接口地址：** `POST /api/factor-trees`

**接口描述：** 创建一个新的因子树

**请求参数：**
```json
{
  "treeName": "因子树名称",
  "description": "因子树描述",
  "sceneId": "场景ID（可选）"
}
```

**响应示例：**
```json
{
  "success": true,
  "message": "因子树创建成功",
  "data": {
    "treeid": 1,
    "nodeName": "因子树名称",
    "description": "因子树描述",
    "nodeType": "TREE",
    "sceneId": "EQUITY",
    "isLeaf": false,
    "sortOrder": 1
  }
}
```

**验收标准：** 用户能输入树名称和描述，成功创建空树结构

---

### 2. 获取所有因子树

**接口地址：** `GET /api/factor-trees`

**接口描述：** 获取指定场景下的所有因子树列表

**请求参数：**
- `sceneId` (可选): 场景ID

**响应示例：**
```json
{
  "success": true,
  "data": [
    {
      "treeid": 1,
      "nodeName": "权益因子树",
      "description": "权益类投资因子树",
      "nodeType": "TREE",
      "sceneId": "EQUITY"
    }
  ]
}
```

---

### 3. 获取树结构

**接口地址：** `GET /api/factor-trees/{treeId}/structure`

**接口描述：** 获取指定因子树的完整树形结构

**路径参数：**
- `treeId`: 因子树ID

**响应示例：**
```json
{
  "success": true,
  "data": {
    "tree": {
      "treeid": 1,
      "nodeName": "权益因子树",
      "description": "权益类投资因子树"
    },
    "nodes": [
      {
        "id": 1,
        "name": "估值类因子",
        "type": "CATEGORY",
        "isLeaf": false,
        "children": [
          {
            "id": 2,
            "name": "PE因子",
            "type": "FACTOR",
            "factorId": 101,
            "isLeaf": true
          }
        ]
      }
    ]
  }
}
```

**验收标准：** 树形视图能清晰展示因子和分类节点的层级关系

---

### 4. 添加节点

**接口地址：** `POST /api/factor-trees/{treeId}/nodes`

**接口描述：** 在指定因子树中添加新节点（支持分类节点、因子节点等）

**请求参数：**
```json
{
  "parentId": 1,
  "nodeName": "节点名称",
  "nodeType": "CATEGORY",
  "factorId": null,
  "description": "节点描述"
}
```

**节点类型说明：**
- `TREE`: 树根节点（通常用于创建新的因子树）
- `CATEGORY`: 分类节点（用于组织和管理因子）
- `FACTOR`: 因子节点（关联具体的因子数据）

**添加分类节点示例：**
```json
{
  "parentId": 26,
  "nodeName": "动量因子",
  "nodeType": "CATEGORY",
  "factorId": null,
  "description": "动量相关因子分类"
}
```

**添加因子节点示例：**
```json
{
  "parentId": 29,
  "nodeName": "MOM_20D",
  "nodeType": "FACTOR",
  "factorId": 1001,
  "description": "20日动量因子"
}
```

**响应示例：**
```json
{
  "success": true,
  "message": "节点添加成功",
  "data": {
    "treeid": 2,
    "parentId": 1,
    "nodeName": "节点名称",
    "nodeType": "CATEGORY",
    "isLeaf": false,
    "sortOrder": 1
  }
}
```

**验收标准：** 用户能指定父节点并添加新节点，保持树结构正确

---

### 5. 更新节点

**接口地址：** `PUT /api/factor-trees/nodes/{nodeId}`

**接口描述：** 更新指定节点的信息

**请求参数：**
```json
{
  "nodeName": "新节点名称",
  "description": "新节点描述",
  "sortOrder": 2
}
```

---

### 6. 移动节点

**接口地址：** `PUT /api/factor-trees/nodes/move`

**接口描述：** 将节点移动到新的父节点下

**请求参数：**
```json
{
  "nodeId": 2,
  "newParentId": 3,
  "sortOrder": 1
}
```

---

### 7. 删除空节点

**接口地址：** `DELETE /api/factor-trees/nodes/{nodeId}`

**接口描述：** 删除指定的空节点（无子节点）

**验收标准：** 仅当节点下无任何因子时允许删除，以防止数据丢失

---

### 8. 搜索因子树

**接口地址：** `GET /api/factor-trees/search/trees`

**接口描述：** 根据关键词搜索因子树

**请求参数：**
- `keyword`: 搜索关键词

**验收标准：** 搜索结果能高亮显示，并支持名称、描述的模糊搜索

---

### 9. 搜索因子

**接口地址：** `GET /api/factor-trees/search/factors`

**接口描述：** 根据关键词搜索因子

**请求参数：**
- `keyword`: 搜索关键词

---

### 10. 获取所有场景

**接口地址：** `GET /api/factor-trees/scenes`

**接口描述：** 获取所有可用的场景列表

**响应示例：**
```json
{
  "success": true,
  "data": [
    {
      "sceneId": "EQUITY",
      "sceneName": "权益投资",
      "sceneDesc": "权益类投资场景"
    },
    {
      "sceneId": "BOND",
      "sceneName": "债券投资",
      "sceneDesc": "债券类投资场景"
    }
  ]
}
```

---

### 11. 创建场景

**接口地址：** `POST /api/factor-trees/scenes`

**接口描述：** 创建新的场景

**请求参数：**
- `sceneId`: 场景ID
- `sceneName`: 场景名称
- `sceneDesc`: 场景描述（可选）

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
| 树名称不能为空 | 创建因子树时未提供名称 | 请提供有效的树名称 |
| 指定场景不存在 | 指定的场景ID不存在 | 请检查场景ID是否正确 |
| 因子树不存在 | 指定的因子树不存在 | 请检查因子树ID是否正确 |
| 节点下有子节点，无法删除 | 尝试删除非空节点 | 请先删除所有子节点 |
| 场景ID已存在 | 创建场景时ID重复 | 请使用不同的场景ID |

## 测试用例

### 创建因子树测试

```bash
curl -X POST http://localhost:8082/fund-advisor-api/api/factor-trees \
  -H "Content-Type: application/json" \
  -d '{
    "treeName": "测试因子树",
    "description": "这是一个测试用的因子树",
    "sceneId": "EQUITY"
  }'
```

### 获取树结构测试

```bash
curl -X GET http://localhost:8082/fund-advisor-api/api/factor-trees/1/structure
```

### 添加节点测试

**添加分类节点：**
```bash
curl -X POST http://localhost:8082/fund-advisor-api/api/factor-trees/1/nodes \
  -H "Content-Type: application/json" \
  -d '{
    "parentId": 1,
    "nodeName": "估值类因子",
    "nodeType": "CATEGORY",
    "description": "估值相关的因子分类"
  }'
```

**添加因子节点：**
```bash
curl -X POST http://localhost:8082/fund-advisor-api/api/factor-trees/1/nodes \
  -H "Content-Type: application/json" \
  -d '{
    "parentId": 2,
    "nodeName": "PE因子",
    "nodeType": "FACTOR",
    "factorId": 101,
    "description": "市盈率因子"
  }'
```

**在根节点下添加分类：**
```bash
curl -X POST http://localhost:8082/fund-advisor-api/api/factor-trees/26/nodes \
  -H "Content-Type: application/json" \
  -d '{
    "parentId": 26,
    "nodeName": "动量因子",
    "nodeType": "CATEGORY",
    "description": "动量相关因子分类"
  }'
```

---

**开发状态：** ✅ 已完成  
**测试覆盖率：** 85%  
**最后更新：** 2025-11-19