# 智能投顾系统 - 架构说明文档

> 本文档说明系统整合后的前后端架构、API路由规范和开发指南

## 📐 系统架构概览

```
┌─────────────────────────────────────────────────────────┐
│                    前端应用 (Frontend)                    │
│                                                           │
│  client-app (http://localhost:5173)                      │
│  ├─ 认证模块 (Login/Register)                            │
│  ├─ Model2 - 因子分析模块                                │
│  ├─ Model3 - 策略组合模块                                │
│  └─ Strategy Console - 策略控制台模块                    │
└─────────────────────────────────────────────────────────┘
                           │
                           │ Vite Proxy
                           ↓
┌─────────────────────────────────────────────────────────┐
│                    后端服务 (Backend)                     │
│                                                           │
│  ┌─────────────────────────────────────────────────┐    │
│  │  factor-backend (8080) - 因子服务               │    │
│  │  路径: /api/factor/**                            │    │
│  └─────────────────────────────────────────────────┘    │
│                                                           │
│  ┌─────────────────────────────────────────────────┐    │
│  │  portfolio-backend (8081) - 组合服务             │    │
│  │  路径: /api/auth/**, /api/funds/**,             │    │
│  │        /api/subscription/**, /combos/**          │    │
│  └─────────────────────────────────────────────────┘    │
│                                                           │
│  ┌─────────────────────────────────────────────────┐    │
│  │  strategy-backend (8082) - 策略服务              │    │
│  │  路径: /api/strategy-**                          │    │
│  └─────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────┘
```

---

## 🗂️ 前端项目结构

### 主应用：client-app

```
frontend/client-app/
├── src/
│   ├── assets/                    # 静态资源
│   ├── components/                # 全局组件
│   │   ├── model1/                # Model1 - 基础基金组件
│   │   ├── model2/                # Model2 - 因子分析组件
│   │   └── model3/                # Model3 - 策略组合组件
│   ├── modules/
│   │   └── strategy-console/      # 策略控制台模块
│   │       ├── components/
│   │       ├── pages/
│   │       ├── router/
│   │       └── utils/
│   ├── views/                     # 页面视图
│   │   ├── auth/                  # 认证页面（登录/注册）
│   │   ├── model2/                # 因子分析页面
│   │   ├── model3/                # 策略组合页面
│   │   └── subscription/          # 订购相关页面
│   ├── router/                    # 路由配置
│   ├── stores/                    # 状态管理（Pinia）
│   ├── config/                    # 配置文件
│   │   └── api.ts                 # API端点配置
│   ├── utils/                     # 工具函数
│   │   └── request.ts             # Axios请求封装
│   └── main.ts                    # 应用入口
├── vite.config.ts                 # Vite配置（包含代理配置）
└── package.json
```

### 其他前端项目（已归档，功能已集成到client-app）

- **Investment Advisor**: 因子管理独立版（功能重复）
- **strategy-console**: 策略控制台独立版（功能重复）

> ⚠️ 这两个项目已经不再维护，所有功能都在 client-app 中实现

---

## 🔧 后端项目结构

```
backend/
├── common-lib/                    # 公共库
├── factor-backend/                # 因子服务 (8080)
│   ├── src/main/java/.../controller/factor/
│   │   ├── FactorBaseController.java
│   │   ├── FactorTreeController.java
│   │   ├── FactorManagementController.java
│   │   ├── StyleTagController.java
│   │   └── FactorValidationController.java
│   └── src/main/resources/
│       └── application.yml        # 端口: 8080
├── portfolio-backend/             # 组合服务 (8081)
│   ├── src/main/java/.../controller/
│   │   ├── AuthController.java
│   │   ├── FundController.java
│   │   ├── StrategyCombinationController.java
│   │   ├── SubscriptionController.java
│   │   └── ...
│   └── src/main/resources/
│       └── application.properties # 端口: 8081
└── strategy-backend/              # 策略服务 (8082)
    ├── src/main/java/.../controller/
    │   ├── StrategyManageController.java
    │   ├── StrategyMonitoringController.java
    │   ├── StrategyRebalanceController.java
    │   └── StrategyWarningController.java
    └── src/main/resources/
        └── application.properties # 端口: 8082
```

---

## 🚀 API 路由规范

### 📌 factor-backend (端口: 8080)

| 路径 | 说明 | Controller |
|------|------|------------|
| `/api/factor/base/**` | 基础因子管理 | FactorBaseController |
| `/api/factor/factor-trees/**` | 因子树管理 | FactorTreeController |
| `/api/factor/factor-management/**` | 衍生因子管理 | FactorManagementController |
| `/api/factor/style-tags/**` | 风格标签管理 | StyleTagController |
| `/api/factor/validation/**` | 因子检验 | FactorValidationController |

**实际访问地址**: `http://localhost:8080/api/factor/**`

---

### 📌 portfolio-backend (端口: 8081)

| 路径 | 说明 | Controller |
|------|------|------------|
| `/api/auth/**` | 认证（登录/注册/登出） | AuthController |
| `/api/funds/**` | 基金查询 | FundController |
| `/api/strategy-combination/**` | 策略组合管理 | StrategyCombinationController |
| `/api/subscription/**` | 订购管理 | SubscriptionController |
| `/api/strategies/**` | 策略列表 | StrategyController |
| `/api/investor/**` | 投资者信息 | InvestorController |
| `/api/risk-assessment/**` | 风险评估 | RiskAssessmentController |
| `/api/agreements/**` | 协议管理 | AgreementController |
| `/api/llm` | LLM智能投顾 | LlmController |
| `/combos/**` | 组合产品（无/api前缀） | ComboController |

**实际访问地址**: `http://localhost:8081/api/**` 或 `http://localhost:8081/combos/**`

---

### 📌 strategy-backend (端口: 8082)

| 路径 | 说明 | Controller |
|------|------|------------|
| `/api/factors` | 因子列表（策略创建用） | FactorController |
| `/api/strategy-management/**` | 策略管理 | StrategyManageController |
| `/api/strategy-monitoring/**` | 策略监控 | StrategyMonitoringController |
| `/api/strategy-rebalance/**` | 策略再平衡 | StrategyRebalanceController |
| `/api/strategy-warning/**` | 策略预警 | StrategyWarningController |
| `/api/python-data/**` | Python数据服务 | PythonDataController |

**实际访问地址**: `http://localhost:8082/api/**`

**注意**: `/api/factor` (单数) 和 `/api/factors` (复数) 是不同的接口：
- `/api/factor/**` → factor-backend (8080) - 因子管理相关接口
- `/api/factors` → strategy-backend (8082) - 策略创建时获取因子列表

---

## 🔐 认证系统架构

### 统一认证方式：Session 认证

系统采用 **Spring Session JDBC** 实现跨服务 Session 共享，确保用户只需登录一次即可访问所有后端服务。

### 认证流程

```
用户登录
  ↓
调用 portfolio-backend /api/auth/login
  ↓
验证用户名和密码
  ↓
将用户信息存储到 Session (MySQL)
  ↓
返回 Session Cookie 到前端
  ↓
前端后续请求携带 Session Cookie
  ↓
所有后端服务共享同一个 Session (通过 JDBC)
  ↓
认证通过
```

### Session 共享配置

**三个后端服务统一配置**:

```properties
# application.properties
spring.session.store-type=jdbc
spring.session.timeout=1800
spring.session.jdbc.initialize-schema=always
```

**数据库**: 所有服务使用同一个数据库 `wealthadvisor`，Spring Session 会自动创建 `SPRING_SESSION` 表存储 Session 数据。

### 认证接口

| 服务 | 接口路径 | 说明 |
|------|---------|------|
| portfolio-backend | `POST /api/auth/login` | 用户登录（主要） |
| portfolio-backend | `POST /api/auth/register` | 用户注册 |
| portfolio-backend | `GET /api/auth/user` | 获取当前用户信息 |
| portfolio-backend | `POST /api/auth/logout` | 用户登出 |
| strategy-backend | `GET /api/strategy-auth/me` | 获取当前用户（内部使用） |

### strategy-backend 认证拦截器

`AuthInterceptor` 支持两种认证方式（按优先级）：

1. **Session 认证**（优先）
   - 检查 Session 中是否有 `userId`, `username`, `role`
   - 如果存在则认证通过

2. **Token 认证**（向后兼容）
   - 检查 `Authorization: Bearer <token>` 头
   - 验证 token 有效性
   - 将 token 信息存储到 Session

```java
// 优先检查 Session
HttpSession session = request.getSession(false);
if (session != null && session.getAttribute("userId") != null) {
    return true;  // Session 认证通过
}

// 如果 Session 无效，再检查 Token
String authHeader = request.getHeader("Authorization");
// ...验证 token
```

### 前端认证配置

**client-app** (`src/utils/request.ts`):

```typescript
const request = axios.create({
  baseURL: '',
  timeout: 10000,
  withCredentials: true,  // 重要：允许发送 Cookie（Session）
  headers: {
    'Content-Type': 'application/json'
  }
})
```

**关键配置**: `withCredentials: true` 允许跨域请求携带 Cookie，实现 Session 共享。

---

## 🔀 前端代理配置

前端通过 Vite 的代理功能将请求路由到不同的后端服务：

**配置文件**: `frontend/client-app/vite.config.ts`

```typescript
const proxy = {
  // factor-backend (8080)
  '/api/factor': { target: 'http://localhost:8080', changeOrigin: true },

  // portfolio-backend (8081)
  '/api/auth': { target: 'http://localhost:8081', changeOrigin: true },
  '/api/funds': { target: 'http://localhost:8081', changeOrigin: true },
  '/api/subscription': { target: 'http://localhost:8081', changeOrigin: true },
  '/api/strategy-combination': { target: 'http://localhost:8081', changeOrigin: true },
  '/combos': { target: 'http://localhost:8081', changeOrigin: true },
  // ... 其他 portfolio-backend 路径

  // strategy-backend (8082)
  '/api/factors': { target: 'http://localhost:8082', changeOrigin: true },
  '/api/strategy-management': { target: 'http://localhost:8082', changeOrigin: true },
  '/api/strategy-monitoring': { target: 'http://localhost:8082', changeOrigin: true },
  '/api/strategy-rebalance': { target: 'http://localhost:8082', changeOrigin: true },
  '/api/strategy-warning': { target: 'http://localhost:8082', changeOrigin: true },
  '/api/python-data': { target: 'http://localhost:8082', changeOrigin: true }
}
```

### 代理工作流程

```
前端请求: http://localhost:5173/api/factor/base/query
    ↓ (Vite代理匹配 /api/factor)
转发到: http://localhost:8080/api/factor/base/query
    ↓ (factor-backend处理)
返回响应
```

---

## 🌐 CORS 配置

所有后端服务统一配置 CORS，允许以下前端地址：

```java
allowedOriginPatterns(
    "http://localhost:5173",      // client-app 开发端口
    "http://localhost:5174",      // 备用端口
    "http://localhost:8081",      // strategy-console 开发端口
    "http://127.0.0.1:5173",
    "http://127.0.0.1:5174"
)
```

**配置文件**:
- `backend/factor-backend/src/main/java/.../config/CorsConfig.java`
- `backend/portfolio-backend/src/main/java/.../config/CorsConfig.java`
- `backend/portfolio-backend/src/main/java/.../config/SecurityConfig.java`
- `backend/strategy-backend/src/main/java/.../config/CorsConfig.java`

---

## 🛠️ 开发指南

### 前端开发

1. **安装依赖**
   ```bash
   cd frontend/client-app
   npm install
   ```

2. **启动开发服务器**
   ```bash
   npm run dev
   ```
   访问: `http://localhost:5173`

3. **构建生产版本**
   ```bash
   npm run build
   ```

### 后端开发

1. **启动所有后端服务** (按顺序启动)

   ```bash
   # 终端1: 启动 factor-backend (8080)
   cd backend/factor-backend
   mvn spring-boot:run

   # 终端2: 启动 portfolio-backend (8081)
   cd backend/portfolio-backend
   mvn spring-boot:run

   # 终端3: 启动 strategy-backend (8082)
   cd backend/strategy-backend
   mvn spring-boot:run
   ```

2. **验证服务启动**
   - factor-backend: `http://localhost:8080/api/factor/...`
   - portfolio-backend: `http://localhost:8081/api/auth/...`
   - strategy-backend: `http://localhost:8082/api/strategy-management/...`

---

## 📝 API 调用示例

### 前端调用示例

```typescript
// src/config/api.ts
export const API_CONFIG = {
  ENDPOINTS: {
    // factor-backend
    FACTOR_BASE_QUERY: '/api/factor/base/query',

    // portfolio-backend
    AUTH_LOGIN: '/api/auth/login',
    FUNDS_LIST: '/api/funds',
    STRATEGY_COMBINATION_ALL: '/combos',

    // strategy-backend
    STRATEGY_MANAGEMENT_LIST: '/api/strategy-management/list'
  }
}

// 使用示例
import request from '@/utils/request'
import { API_CONFIG } from '@/config/api'

// 登录
const login = async (username: string, password: string) => {
  const response = await request.post(API_CONFIG.ENDPOINTS.AUTH_LOGIN, {
    username,
    password
  })
  return response.data
}

// 查询因子
const queryFactors = async () => {
  const response = await request.get(API_CONFIG.ENDPOINTS.FACTOR_BASE_QUERY)
  return response.data
}
```

---

## ⚠️ 重要变更说明

### ✅ 已完成的整合

1. **后端API路径规范化**
   - 移除了 factor-backend 的 `context-path: /api`
   - 所有 Controller 统一添加 `/api` 前缀
   - API路径清晰、无冗余

2. **统一CORS配置**
   - 三个后端服务使用一致的CORS策略
   - 修复了 portfolio-backend 的重复CORS配置冲突
   - 统一允许前端开发端口访问

3. **前端代理配置优化**
   - 移除了模糊的 `/api` 兜底规则
   - 明确配置每个API路径的路由目标
   - 避免代理冲突

4. **前端项目整合**
   - client-app 作为唯一的前端应用
   - Investment Advisor 和 strategy-console 已归档
   - 所有功能统一在 client-app 中维护

### 🔄 迁移路径（如果需要）

如果需要从旧版本迁移到新架构：

1. **前端迁移**
   - 使用 `frontend/client-app` 作为主应用
   - 更新依赖: `npm install`
   - 验证路由和API调用

2. **后端迁移**
   - 重新启动三个后端服务
   - 验证API端点可访问性
   - 检查CORS是否正常工作

---

## 🧪 测试指南

### 测试前端代理是否正常

1. 启动所有后端服务
2. 启动前端开发服务器
3. 在浏览器控制台查看网络请求
4. 验证请求是否正确代理到对应后端

### 测试CORS配置

```bash
# 测试 factor-backend
curl -X OPTIONS http://localhost:8080/api/factor/base/query \
  -H "Origin: http://localhost:5173" \
  -H "Access-Control-Request-Method: GET"

# 应返回 Access-Control-Allow-Origin: http://localhost:5173
```

---

## 📞 常见问题

### Q1: 为什么要移除 factor-backend 的 context-path?

**A**: `context-path: /api` 会导致实际路径变成 `/api/api/factor/...`，造成混乱。移除后统一在 Controller 层添加 `/api` 前缀更清晰。

### Q2: 前端代理配置中为什么没有 `/api` 兜底规则?

**A**: 兜底规则太模糊，会导致路由冲突。明确配置每个路径可以精确控制请求路由。

### Q3: Investment Advisor 和 strategy-console 还能用吗?

**A**: 这两个项目的功能已经完全集成到 client-app 中，建议使用 client-app。独立项目仅作为参考保留。

### Q4: 如何添加新的API端点?

**A**:
1. 在对应后端的 Controller 中添加接口
2. 在 `frontend/client-app/vite.config.ts` 中添加代理规则（如需要）
3. 在 `frontend/client-app/src/config/api.ts` 中添加端点配置

### Q5: 策略运营模块点击后重定向到登录页，怎么办？

**A**: 这是因为 strategy-backend 原本使用 Token 认证，现在已修复为支持 Session 认证。

**问题原因**:
- portfolio-backend 使用 Session 认证
- strategy-backend 使用 Token 认证（AuthInterceptor 强制要求 Bearer Token）
- 用户登录后只有 Session，没有 Token
- 访问 strategy-backend 接口时返回 401，前端重定向到登录页

**解决方案**:
已修改 `strategy-backend` 的 `AuthInterceptor`，优先检查 Session，如果 Session 有效则认证通过。通过 Spring Session JDBC 实现跨服务 Session 共享。

**修复步骤**:
1. 重新编译并启动 strategy-backend
2. 清除浏览器缓存和 Cookie
3. 重新登录
4. 现在访问策略运营模块应该正常工作

### Q6: 为什么 strategy-backend 有两个 auth 接口？

**A**: 历史遗留问题，现在已规范化：

- **portfolio-backend** 的 `/api/auth/**` - 主要认证接口（登录、注册、登出）
- **strategy-backend** 的 `/api/strategy-auth/me` - 内部认证检查（获取当前用户）
- **strategy-backend** 的 `/api/test` - Token 登录接口（已废弃，建议使用 Session 认证）

前端代理 `/api/auth` 会路由到 portfolio-backend，不会冲突。

### Q7: `/api/factor` 和 `/api/factors` 有什么区别？

**A**: 这是两个不同的接口，服务于不同的业务场景：

| 接口 | 后端服务 | 用途 | 说明 |
|------|---------|------|------|
| `/api/factor/**` | factor-backend (8080) | 因子管理 | 完整的因子CRUD操作，如因子树管理、因子创建、因子验证等 |
| `/api/factors` | strategy-backend (8082) | 策略创建 | 仅获取因子列表，用于策略创建时选择因子 |

**为什么有两个**？
- factor-backend 是专门的因子管理服务，提供复杂的因子分析功能
- strategy-backend 需要一个简单的因子列表接口，用于策略创建
- 避免跨服务调用，提高性能

**响应格式**：
- `/api/factor/base/query` (factor-backend): 详细的因子信息，包含历史数据、IC/IR等
- `/api/factors` (strategy-backend): 简化的因子列表，仅包含 id, name, code, description

---

## 📚 相关文档

- [Vue 3 官方文档](https://vuejs.org/)
- [Vite 官方文档](https://vitejs.dev/)
- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [Element Plus 组件库](https://element-plus.org/)

---

## 🤝 贡献指南

1. Fork 本仓库
2. 创建特性分支: `git checkout -b feature/your-feature`
3. 提交更改: `git commit -m 'Add some feature'`
4. 推送到分支: `git push origin feature/your-feature`
5. 提交 Pull Request

---

## 📄 License

MIT License

---

**最后更新**: 2025-01-26
**维护者**: 智能投顾开发团队
