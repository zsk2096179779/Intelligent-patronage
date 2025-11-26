# 各页面 API 请求总结

## 1. 登录页面 (`index.vue`)

### API 请求
- **POST** `/api/test`
  - **功能**: 用户登录认证
  - **请求参数**: 
    ```json
    {
      "id": 1,
      "username": "用户名"
    }
    ```
  - **返回**: token、username、success
  - **用途**: 用户登录，获取访问令牌

---

## 2. 主布局页面 (`Leader.vue`)

### API 请求
- **GET** `/api/auth/me`
  - **功能**: 获取当前登录用户信息
  - **请求参数**: 无（通过Session）
  - **返回**: userId、username、role、success
  - **用途**: 页面初始化时获取当前用户信息，显示用户名

---

## 3. 策略管理页面 (`StrategyManagement.vue`)

### API 请求

#### 3.1 获取用户信息
- **GET** `/api/auth/me`
  - **功能**: 获取当前登录用户
  - **用途**: 验证用户登录状态

#### 3.2 获取策略列表
- **POST** `/api/strategy-management`
  - **功能**: 获取所有策略列表
  - **请求参数**: 
    ```json
    {
      "id": 1,
      "username": "用户名"
    }
    ```
  - **返回**: 策略数组（包含 id、name、type、status、createdAt、riskLevel）
  - **用途**: 显示策略列表

#### 3.3 启动策略
- **POST** `/api/strategy-management/start`
  - **功能**: 启动指定策略
  - **请求参数**: 
    ```json
    {
      "strategyId": 策略ID
    }
    ```
  - **返回**: success、message
  - **用途**: 将策略状态改为 running

#### 3.4 停止策略
- **POST** `/api/strategy-management/stop`
  - **功能**: 停止指定策略
  - **请求参数**: 
    ```json
    {
      "strategyId": 策略ID
    }
    ```
  - **返回**: success、message
  - **用途**: 将策略状态改为 stop

#### 3.5 删除策略
- **POST** `/api/strategy-management/delete`
  - **功能**: 删除指定策略
  - **请求参数**: 
    ```json
    {
      "strategyId": 策略ID
    }
    ```
  - **返回**: success、message
  - **用途**: 删除策略及其关联数据

---

## 4. 策略创建页面 (`StrategyCreation.vue`)

### API 请求

#### 4.1 获取因子列表
- **GET** `/api/factors`
  - **功能**: 获取可用因子列表
  - **请求参数**: 无
  - **返回**: 因子数组（包含 id、name、code、description）
  - **用途**: 在创建策略时选择因子

#### 4.2 创建策略
- **POST** `/api/strategy-management/new`
  - **功能**: 创建新策略
  - **请求参数**: 
    ```json
    {
      "name": "策略名称",
      "type": "策略类型",
      "riskLevel": 风险等级(1-5),
      "description": "策略描述",
      "factors": [
        {
          "factorName": "因子名称",
          "weight": 权重(数字),
          "frequency": "daily|weekly|monthly"
        }
      ],
      "filterRules": [
        {
          "ruleType": "top_n:10" 或 "type_limit:类型" 或 "scale_limit:限制"
        }
      ]
    }
    ```
  - **返回**: success、strategyId
  - **用途**: 创建新策略，包括基本信息、因子配置和选基规则

---

## 5. 策略详情页面 (`Detail.vue`)

### API 请求

#### 5.1 获取策略详情
- **POST** `/api/strategy-management/Detail`
  - **功能**: 获取策略详细信息
  - **请求参数**: 
    ```json
    {
      "strategyId": 策略ID
    }
    ```
  - **返回**: 
    ```json
    {
      "strategy": {
        "id": 策略ID,
        "name": "策略名称",
        "type": "策略类型",
        "description": "描述",
        "riskLevel": 风险等级,
        "status": "状态",
        "createdAt": "创建时间",
        "updatedAt": "修改时间"
      },
      "factors": [
        {
          "id": 因子ID,
          "factorName": "因子名称",
          "weight": 权重,
          "frequency": "计算频率"
        }
      ],
      "filterRules": [
        {
          "id": 规则ID,
          "ruleType": "规则类型"
        }
      ]
    }
    ```
  - **用途**: 展示策略的完整信息，包括基本信息、因子配置和选基规则

---

## 6. 策略监控页面 (`StrategyMonitoring.vue`)

### API 请求

#### 6.1 获取策略列表
- **POST** `/api/strategy-management`
  - **功能**: 获取策略列表（用于下拉选择）
  - **请求参数**: 
    ```json
    {
      "id": 1,
      "username": "用户名"
    }
    ```
  - **返回**: 策略数组
  - **用途**: 策略选择下拉框

#### 6.2 获取监控指标
- **POST** `/api/strategy-monitoring/Metrics`
  - **功能**: 获取策略监控指标
  - **请求参数**: 
    ```json
    {
      "id": 策略ID
    }
    ```
  - **返回**: 监控指标数据（系统状态、运行天数、风险等级等）
  - **用途**: 显示策略监控指标

#### 6.3 获取预警信息
- **POST** `/api/strategy-monitoring/Warnings`
  - **功能**: 获取策略预警信息
  - **请求参数**: 
    ```json
    {
      "id": 策略ID
    }
    ```
  - **返回**: 预警信息数组
  - **用途**: 显示策略预警列表

#### 6.4 获取收益曲线
- **POST** `/api/strategy-monitoring/ProfitCurve`
  - **功能**: 获取策略收益曲线数据
  - **请求参数**: 
    ```json
    {
      "id": 策略ID
    }
    ```
  - **返回**: 收益曲线数据点数组
  - **用途**: 绘制收益曲线图表

#### 6.5 获取热力图数据
- **POST** `/api/strategy-monitoring/Heatmap`
  - **功能**: 获取策略热力图数据
  - **请求参数**: 
    ```json
    {
      "id": 策略ID
    }
    ```
  - **返回**: 热力图数据
  - **用途**: 显示策略热力图

---

## 7. 配置管理页面 (`RebalanceSetting.vue`)

### API 请求

#### 7.1 获取策略列表
- **POST** `/api/strategy-management`
  - **功能**: 获取策略列表（用于下拉选择）
  - **请求参数**: 
    ```json
    {
      "id": 1,
      "name": "用户名"
    }
    ```
  - **返回**: 策略数组
  - **用途**: 策略选择下拉框

#### 7.2 获取策略监控指标
- **POST** `/api/strategy-monitoring/Metrics`
  - **功能**: 获取策略基本信息（用于显示策略类型、运行时间、风险等级）
  - **请求参数**: 
    ```json
    {
      "id": 策略ID,
      "name": "用户名"
    }
    ```
  - **返回**: 策略监控指标
  - **用途**: 显示策略基本信息

#### 7.3 获取再平衡配置
- **POST** `/api/strategy-rebalance/Detail`
  - **功能**: 获取策略的再平衡配置
  - **请求参数**: 
    ```json
    {
      "id": 策略ID,
      "name": "用户名"
    }
    ```
  - **返回**: 
    ```json
    {
      "id": 配置ID,
      "strategyId": 策略ID,
      "rebalancePeriod": "weekly|monthly|quarterly",
      "maxRebalanceRatio": 最大调仓比例(1-100),
      "isActiveRebalance": true|false,
      "createdAt": "创建时间",
      "updatedAt": "更新时间"
    }
    ```
  - **用途**: 加载并显示再平衡配置

#### 7.4 更新再平衡配置
- **POST** `/api/strategy-rebalance/Update`
  - **功能**: 保存再平衡配置
  - **请求参数**: 
    ```json
    {
      "strategyId": 策略ID,
      "rebalancePeriod": "weekly|monthly|quarterly",
      "maxRebalanceRatio": 最大调仓比例(1-100),
      "isActiveRebalance": true|false
    }
    ```
  - **返回**: success、message
  - **用途**: 保存再平衡配置更改

#### 7.5 执行回测
- **POST** `/api/strategy-rebalance/HuiCe`
  - **功能**: 执行策略回测
  - **请求参数**: 
    ```json
    {
      "strategyId": 策略ID,
      "startDate": "开始日期(YYYY-MM-DD)",
      "endDate": "结束日期(YYYY-MM-DD)",
      "initialCapital": 初始资金,
      "transactionFee": 交易费率,
      "slippage": 滑点
    }
    ```
  - **返回**: 
    ```json
    {
      "cumulativeReturn": 累计收益,
      "maxDrawdown": 最大回撤,
      "sharpeRatio": 夏普比率,
      "trades": 交易次数
    }
    ```
  - **用途**: 执行策略回测并显示结果

---

## API 端点汇总

### 认证相关
- `GET /api/auth/me` - 获取当前用户信息
- `POST /api/test` - 用户登录

### 策略管理相关
- `POST /api/strategy-management` - 获取策略列表
- `POST /api/strategy-management/new` - 创建策略
- `POST /api/strategy-management/start` - 启动策略
- `POST /api/strategy-management/stop` - 停止策略
- `POST /api/strategy-management/delete` - 删除策略
- `POST /api/strategy-management/Detail` - 获取策略详情

### 策略监控相关
- `POST /api/strategy-monitoring/Metrics` - 获取监控指标
- `POST /api/strategy-monitoring/Warnings` - 获取预警信息
- `POST /api/strategy-monitoring/ProfitCurve` - 获取收益曲线
- `POST /api/strategy-monitoring/Heatmap` - 获取热力图

### 再平衡配置相关
- `POST /api/strategy-rebalance/Detail` - 获取再平衡配置
- `POST /api/strategy-rebalance/Update` - 更新再平衡配置
- `POST /api/strategy-rebalance/HuiCe` - 执行回测

### 因子相关
- `GET /api/factors` - 获取因子列表

---

## 注意事项

1. **认证方式**: 所有API请求（除 `/api/test`）都需要在请求头中携带 `Authorization: Bearer <token>`
2. **Session验证**: 后端会验证Session中的userId，确保用户已登录
3. **错误处理**: 401错误会自动重定向到登录页
4. **数据格式**: 所有POST请求使用JSON格式，Content-Type为 `application/json`

