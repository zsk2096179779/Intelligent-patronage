# 智能投顾一体化整合方案

## 1. 现状与定位

- `fund-advisor/fund-advisor`：因子接入、因子树管理、衍生因子构建以及大类资产策略研究的服务端，专注于研究与数据层能力。功能详见“因子接入/因子树管理/策略管理”等描述。  
- `Intelligent-patronage-back/Intelligent-patronage-back`：以 Spring Boot/MyBatis 实现的组合产品编排与审核后台，涵盖组合创建、产品参数配置、持仓管理与审核流（`/api/strategy-combination`, `/api/funds` 等接口）。  
- `Intelligent-patronage-lyb-front/Intelligent-patronage-zsk-front`：Vue 3 + TS + Element Plus 前端，提供仪表盘、组合、市场分析、智能顾问与系统设置等交互层能力。
- `shixun-main/shixun-main`：已实现策略管理/监控/再平衡/AI助手等功能的前后端分离系统，可作为统一系统的骨架与运行时壳体。

> 目标：把研究（因子）、组合产品编排、策略运营/前端体验整合为一个面向投研 + 业务 + 客户的完整智能投顾系统。

## 2. 目标架构

```
integrated-system/
├── services/
│   ├── factor-service        # 由 fund-advisor 演进
│   ├── portfolio-service     # 由 Intelligent-patronage-back 演进
│   ├── strategy-service      # 由 shixun-main backend 演进
│   ├── auth-service          # 提炼统一认证/会话
│   └── api-gateway           # 统一入口 (Spring Cloud Gateway 或 Nginx)
├── frontend/
│   └── client-app            # 基于 Intelligent-patronage-lyb-front + shixun-main 前端整合
├── python/
│   └── analytics             # Python脚本/模型（来自 shixun-main/python & fund-advisor 脚本）
├── infra/
│   ├── docker-compose.yml    # MySQL、Redis、Session DB、消息队列
│   └── k8s/helm              #（可选）容器化部署
└── docs/
    └── SYSTEM_INTEGRATION_PLAN.md
```

关键原则：
1. **分层**：前端聚合层、API 网关、领域微服务（因子/策略/组合）、数据/AI 服务。
2. **统一鉴权**：提炼 `auth-service`（或复用 `strategy-service` 内的 Session 逻辑）以支持 SSO + Token，前端只需一套登录。
3. **共享域模型**：通过 `common-domain` 库共享用户、策略、组合、因子等实体与 DTO，避免重复定义。
4. **可独立部署**：每个服务仍可独立开发测试，通过消息或 REST 互通。

## 3. 后端整合路径

### 阶段 A：代码与依赖统一
- 统一 Java 版本（17）与 Spring Boot 版本（>3.2），在根 Pom 建立父模块，三个服务作为子模块。
- 抽离公共依赖（MyBatis、Spring Session、fastjson/Jackson、自定义 starter）。

### 阶段 B：认证与网关
- 将 `shixun-main/backend` 中的 Session/拦截器逻辑提炼为 `auth-service`，提供登录、Session 校验、Token 签发。
- 引入 Spring Cloud Gateway 或 Nginx 进行路由：  
  `/api/factors/**` → `factor-service`  
  `/api/portfolios/**` → `portfolio-service`  
  `/api/strategies/**` → `strategy-service`
- 配置统一的跨域、日志、熔断、限流。

### 阶段 C：领域服务拆分与协作
1. **因子服务（factor-service）**
   - 暴露因子树、衍生因子、因子校验接口。
   - 增加事件/消息推送（Kafka/Redis Stream）向策略服务通知新因子或指标变动。
2. **策略服务（strategy-service）**
   - 保留原策略管理/监控/再平衡接口。
   - 新增因子数据的读取适配层（通过 REST/gRPC 调用 factor-service）。
   - 负责 Python 任务触发（test.py/test2.py 等）并将结果持久化。
3. **组合服务（portfolio-service）**
   - 保留组合创建/审核/持仓接口。
   - 对接策略服务：创建组合时读取策略列表、引用 strategyId/RefId。
   - 对接因子服务：组合持仓可附带对应因子暴露，为前端展示提供扩展数据。

### 阶段 D：数据层与同步
- 设计统一数据库逻辑库：  
  - `core_user`, `strategy_*`, `factor_*`, `portfolio_*`.  
- 通过 Flyway/Liquibase 管理三个系统的建表脚本并补齐外键/索引。
- 若短期无法统一，采用多数据源配置：  
  - 在 `strategy-service` 中配置因子数据源（只读）+ 业务库。  
  - 通过 `DataSourceRouting` 保持兼容，逐步迁移。
- 建立数据契约（API + 数据字典）放在 `docs/data-contracts`.

### 阶段 E：编排与自动化
- 编写 `docker-compose.yml`：启动 MySQL、Redis、MinIO（若需要）、三个服务、前端与 Python worker。
- 在 `infra/ci` 添加 GitHub Actions / GitLab CI pipeline：  
  - 构建三个后端 Jar + 前端包。  
  - 执行单元测试与 Lint（maven test、npm test、pytest）。

## 4. 前端整合方案

1. **单一入口应用**
   - 以 `Intelligent-patronage-lyb-front` 为基础，迁入 `shixun-main/frontend` 的页面（策略管理/监控/再平衡）与组件（AI 助手等）。
   - 重新划分路由：`/dashboard`, `/strategies`, `/portfolios`, `/analysis`, `/advisor`, `/settings`.
2. **API 适配层**
   - 在 `src/utils/http.ts` 中统一 Axios 实例，指向网关 `/api`.
   - 建立 `services/strategy.ts`, `services/factor.ts`, `services/portfolio.ts`。
3. **状态管理**
   - 通过 Pinia store 管理用户、策略、因子、组合数据。
   - 登录后存储 Token + 用户信息，与后端 auth-service 对接。
4. **UI/UX 融合**
   - 复用 Element Plus 主题，整合 shixun-main 的页面布局与 lyb-front 的仪表盘。
   - 引入可插拔模块（因子树、组合审核看板、策略监控大屏）。

## 5. Python/AI 能力整合

- 将 `shixun-main/python` 与 `fund-advisor` 中的脚本汇聚到 `python/analytics`，标准化入口（如 `python -m analytics.factor_heatmap --strategyId=xxx`）。
- 提供 REST Webhook：策略服务触发脚本后，通过回调/消息更新结果。
- 配置虚拟环境与 requirements.txt，提供调度脚本（Airflow/Celery 可选）。

## 6. 阶段性交付计划

| 阶段 | 里程碑 | 关键产物 |
| ---- | ------ | -------- |
| Sprint 1 | 仓库重构 + 统一构建链路 | Monorepo 结构、父 Pom、前端合并脚手架 |
| Sprint 2 | 认证统一 + 网关上线 | Auth-service、网关、统一登录 |
| Sprint 3 | 因子/策略服务解耦 | REST 合同、公共 DTO、跨服务调用 |
| Sprint 4 | 组合服务对接 + 审核流程贯通 | Portfolio-service 接入策略/因子数据、前端组合工作台 |
| Sprint 5 | Python/AI 集成 + 观测性 | Python 任务编排、日志/metrics/tracing、CI/CD 完成 |

## 7. 风险与缓解

- **数据库差异**：表结构不同 → 使用 DDL 对齐、引入数据迁移脚本。
- **接口契约破裂**：三个系统请求/响应风格不一 → 在整合期通过网关适配器（DTO Mapper）逐步统一。
- **部署复杂度**：多服务 → Docker Compose + Helm chart 先落地测试环境。
- **团队协作**：分模块开发 → 看板拆分、定义 API Mock、每日集成验证。

## 8. 下一步动作

1. 在仓库根目录执行模块化调整并创建父 Pom/顶层 package.json。
2. 评估并导出现有数据库 schema，梳理共享实体。
3. 选定认证方案（Session vs JWT），完成 PoC。
4. 前端合并 PoC：把策略管理与仪表盘两个页面跑通。
5. 制定详细的数据与 API 契约文档，进入开发迭代。


