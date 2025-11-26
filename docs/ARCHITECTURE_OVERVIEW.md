# 项目架构概览

## 📋 项目概述

这是一个**策略管理子系统**，用于管理、监控和配置投资策略。采用前后端分离架构，前端使用 Vue 3，后端使用 Spring Boot。

---

## 🏗️ 技术栈

### 后端 (Backend)
- **框架**: Spring Boot 3.5.7
- **语言**: Java 17
- **数据库**: MySQL (远程数据库: 116.62.82.244:3306)
- **ORM**: MyBatis 3.0.5
- **会话管理**: Spring Session JDBC
- **密码加密**: BCrypt
- **定时任务**: Spring Scheduling (`@EnableScheduling`)
- **外部集成**:
  - DeepSeek LLM API (AI功能)
  - Python脚本执行 (因子计算、数据分析)

### 前端 (Frontend)
- **框架**: Vue 3.5.16
- **构建工具**: Vite 6.3.5
- **UI组件库**: Element Plus 2.10.2
- **路由**: Vue Router 4.5.1
- **状态管理**: Pinia 3.0.3
- **HTTP客户端**: Axios 1.10.0
- **图表库**: ECharts 5.6.0

---

## 📁 项目结构

```
shixun-main/
├── backend/                    # 后端项目
│   ├── src/main/java/com/example/backend/
│   │   ├── BackendApplication.java    # 启动类
│   │   ├── config/                    # 配置类
│   │   │   ├── CorsConfig.java        # CORS跨域配置
│   │   │   ├── MyBatisConfig.java      # MyBatis配置
│   │   │   ├── RestTemplateConfig.java # HTTP客户端配置
│   │   │   └── WebConfig.java          # Web配置
│   │   ├── controller/                # 控制器层 (REST API)
│   │   │   ├── AuthController.java     # 认证相关
│   │   │   ├── StrategyManageController.java    # 策略管理
│   │   │   ├── StrategyMonitoringController.java # 策略监控
│   │   │   ├── StrategyRebalanceController.java  # 再平衡配置
│   │   │   ├── StrategyWarningController.java    # 预警管理
│   │   │   ├── FactorController.java             # 因子管理
│   │   │   ├── PythonDataController.java         # Python数据接口
│   │   │   └── LLMController.java                # AI助手
│   │   ├── service/                    # 业务逻辑层
│   │   │   ├── StrategyService.java
│   │   │   ├── StrategyMonitoringService.java
│   │   │   ├── StrategyWarningService.java
│   │   │   ├── RebalanceBacktestService.java
│   │   │   ├── PythonDataService.java
│   │   │   └── ...
│   │   ├── mapper/                     # MyBatis Mapper接口
│   │   ├── entity/                     # 实体类 (17个)
│   │   │   ├── Strategy.java
│   │   │   ├── StrategyConfig.java
│   │   │   ├── StrategyFactor.java
│   │   │   ├── StrategyFilterRule.java
│   │   │   └── ...
│   │   ├── dto/                        # 数据传输对象
│   │   └── interceptor/                # 拦截器
│   │       └── AuthInterceptor.java
│   └── src/main/resources/
│       ├── application.properties       # 配置文件
│       └── mapper/                      # MyBatis XML映射文件 (16个)
│
├── frontend/                   # 前端项目
│   ├── src/
│   │   ├── main.js                     # 入口文件
│   │   ├── App.vue                     # 根组件
│   │   ├── router/index.js              # 路由配置
│   │   ├── utils/http.js                # HTTP请求封装
│   │   ├── pages/                       # 页面组件
│   │   │   ├── index.vue                # 登录页
│   │   │   ├── Leader.vue              # 主布局 (侧边栏导航)
│   │   │   ├── StrategyManagement.vue   # 策略管理页
│   │   │   ├── StrategyCreation.vue     # 策略创建页
│   │   │   ├── StrategyMonitoring.vue   # 策略监控页
│   │   │   ├── RebalanceSetting.vue     # 再平衡配置页
│   │   │   └── Detail.vue               # 策略详情页
│   │   └── components/                  # 公共组件
│   │       └── AIAssistant.vue         # AI助手组件
│   └── vite.config.js                   # Vite配置
│
└── python/                     # Python脚本
    ├── test2.py                # 基金因子计算脚本
    ├── test.py                 # 新闻预警脚本
    └── heatmap.py              # 热力图生成脚本
```

---

## 🔄 前后端交互流程

### 1. 认证流程
```
前端 (index.vue)
  ↓ POST /api/test
后端 (Login.java)
  ↓ 验证用户 → 创建Session
  ↓ 返回 token
前端
  ↓ 保存 token 到 localStorage
  ↓ 跳转到 /Leader
```

### 2. API请求流程
```
前端页面
  ↓ 调用 http.post('/api/xxx')
  ↓ http.js 拦截器添加 Authorization header
  ↓ Vite代理转发到后端 (http://localhost:8080)
后端 Controller
  ↓ 验证 Session
  ↓ 调用 Service 层
  ↓ 调用 Mapper 层查询数据库
  ↓ 返回 JSON 响应
前端
  ↓ 处理响应数据
  ↓ 更新页面状态
```

### 3. 跨域配置
- **前端代理**: Vite配置将 `/api/*` 代理到 `http://localhost:8080`
- **后端CORS**: `CorsConfig.java` 允许指定源访问
- **凭证传递**: `withCredentials: true` 支持 Cookie/Session

---

## 🗄️ 数据库设计

### 核心实体关系
```
User (用户)
  └─ owns → Strategy (策略)
       ├─ has → StrategyConfig (策略配置)
       ├─ has → StrategyFactor (策略因子)
       ├─ has → StrategyFilterRule (筛选规则)
       ├─ has → StrategyMonitorMetrics (监控指标)
       ├─ has → StrategyWarning (预警信息)
       ├─ has → StrategyHolding (持仓信息)
       ├─ has → StrategyTradeHistory (交易历史)
       └─ has → RebalanceConfig (再平衡配置)
```

### 主要表结构
- **strategy**: 策略基本信息 (id, name, type, status, owner, ...)
- **strategy_config**: 策略配置 (风险等级、再平衡周期等)
- **strategy_factor**: 策略使用的因子配置
- **strategy_filter_rule**: 选基筛选规则
- **strategy_monitor_metrics**: 监控指标数据
- **strategy_warning**: 预警信息
- **rebalance_config**: 再平衡配置
- **rebalance_backtest**: 回测结果

---

## 🎯 核心功能模块

### 1. 策略管理 (`StrategyManageController`)
- **列表查询**: `POST /api/strategy-management` - 获取用户所有策略
- **创建策略**: `POST /api/strategy-management/new` - 创建新策略
- **启动/停止**: `POST /api/strategy-management/start|stop` - 控制策略状态
- **删除策略**: `POST /api/strategy-management/delete` - 删除策略
- **策略详情**: `POST /api/strategy-management/Detail` - 获取策略详细信息
- **收益曲线**: `POST /api/strategy-management/Chart` - 获取收益曲线数据

### 2. 策略监控 (`StrategyMonitoringController`)
- **监控指标**: `POST /api/strategy-monitoring/Metrics` - 获取策略监控指标
- **预警信息**: `POST /api/strategy-monitoring/Warnings` - 获取预警列表
- **收益曲线**: `POST /api/strategy-monitoring/ProfitCurve` - 收益曲线数据
- **热力图**: `POST /api/strategy-monitoring/Heatmap` - 因子热力图数据

### 3. 再平衡配置 (`StrategyRebalanceController`)
- **配置详情**: `POST /api/strategy-rebalance/Detail` - 获取再平衡配置
- **保存配置**: `POST /api/strategy-rebalance/Save` - 保存再平衡设置
- **回测**: `POST /api/strategy-rebalance/HuiCe` - 执行回测

### 4. 因子管理 (`FactorController`)
- 因子基础数据管理
- 因子配置查询

### 5. Python集成 (`PythonDataController`)
- 调用Python脚本进行因子计算
- 处理基金数据 (`fund_factors_result.csv`)

### 6. AI助手 (`LLMController`)
- 集成DeepSeek LLM
- 提供智能问答功能

---

## 🔐 认证与授权

### Session管理
- **存储方式**: Spring Session JDBC (存储在数据库)
- **超时时间**: 30分钟 (1800秒)
- **验证机制**: 每个Controller方法检查 `HttpSession.getAttribute("userId")`

### 权限控制
- **策略拥有者验证**: 只有策略的 `owner` 可以修改/删除策略
- **状态限制**: `paused` 状态的策略不能启动/停止

---

## 📊 数据流示例

### 策略监控数据流
```
前端 StrategyMonitoring.vue
  ↓ 选择策略
  ↓ 并行请求4个API:
     - /strategy-monitoring/Metrics
     - /strategy-monitoring/Warnings
     - /strategy-monitoring/ProfitCurve
     - /strategy-monitoring/Heatmap
  ↓
后端 StrategyMonitoringController
  ↓ 调用 StrategyMonitoringService
  ↓ 查询数据库 (StrategyMonitorMetricsMapper)
  ↓ 返回JSON数据
  ↓
前端
  ↓ 使用 ECharts 渲染图表
  ↓ 显示监控指标、预警、收益曲线、热力图
```

### 预警数据流
```
定时任务 / Python脚本 (test.py)
  ↓ 获取财经新闻
  ↓ 调用AI分析风险
  ↓
后端 StrategyWarningService
  ↓ fetchAndSaveWarnings()
  ↓ 保存到 strategy_warning 表
  ↓
前端 StrategyMonitoring.vue
  ↓ 请求 /strategy-monitoring/Warnings
  ↓ 显示预警列表
```

---

## 🛠️ 开发配置

### 后端配置 (`application.properties`)
```properties
# 数据库
spring.datasource.url=jdbc:mysql://116.62.82.244:3306/wealthadvisor
spring.datasource.username=remote_admin
spring.datasource.password=123456

# MyBatis
mybatis.mapper-locations=classpath:mapper/*.xml

# Session
spring.session.store-type=jdbc
spring.session.timeout=1800

# DeepSeek LLM
deepseek.api.base-url=https://api.deepseek.com
deepseek.api.model=deepseek-chat

# Python
python.executable=D:/develop/Anaconda/python.exe
```

### 前端配置 (`vite.config.js`)
```javascript
server: {
  port: 8081,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

---

## 🚀 启动流程

### 后端启动
```bash
cd backend
mvn spring-boot:run
# 或
java -jar target/train_back-0.0.1-SNAPSHOT.jar
```
- 默认端口: `8080`
- 自动初始化Session表
- 启用定时任务调度

### 前端启动
```bash
cd frontend
npm install
npm run dev
```
- 默认端口: `8081` (可在 `vite.config.js` 配置)
- 开发模式支持热重载
- API请求自动代理到后端

---

## 📝 关键特性

1. **前后端分离**: 完全解耦，通过REST API通信
2. **Session管理**: 使用数据库存储Session，支持集群部署
3. **权限控制**: 基于用户ID和策略拥有者的细粒度权限
4. **Python集成**: 通过 `ProcessBuilder` 调用Python脚本
5. **AI集成**: 集成DeepSeek LLM提供智能分析
6. **实时监控**: 策略状态、指标、预警实时更新
7. **回测功能**: 支持策略回测，评估历史表现

---

## 🔍 数据文件

- **fund_factors_result.csv**: 基金因子计算结果 (7158条记录)
  - 包含: code, name, update_date, nav, nav_acc, dividend_ratio, volatility_20, momentum_20, drawdown, sharpe_20, mean_reversion
  - 由 `python/test2.py` 生成

---

## 📌 注意事项

1. **数据库连接**: 使用远程MySQL数据库，需要网络连接
2. **Python环境**: 需要配置正确的Python可执行文件路径
3. **Session超时**: 30分钟无操作会自动登出
4. **CORS配置**: 开发环境已配置，生产环境需要调整
5. **API路径**: 所有API都以 `/api` 开头
6. **请求方式**: 大部分API使用 `POST` 方法

---

## 🎨 前端路由结构

```
/ (index.vue) - 登录页
  ↓
/Leader - 主布局 (侧边栏)
  ├── /strategy-management - 策略管理
  ├── /strategy-creation - 策略创建
  ├── /strategy-monitoring - 策略监控
  ├── /rebalance-setting - 再平衡配置
  └── /strategy-management/detail/:strategyId - 策略详情
```

---

## 📚 相关文档

- `API_REQUEST_SUMMARY.md` - API请求汇总
- `STRATEGY_MONITORING_LOGIC.md` - 策略监控逻辑说明

