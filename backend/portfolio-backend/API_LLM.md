# 智能投顾（LLM）后端 API 文档

简介：本文档描述后端为前端代理 LLM 请求时提供的接口约定、配置项、安全与调试说明。

接口
- URL: `POST /api/llm`
- 请求体 (JSON):

  {
    "messages": [
      { "role": "user|assistant|system", "content": "..." }
    ],
    "options": { /* 可选，传给上游 provider 的额外选项 */ }
  }

- 响应体 (JSON):

  {
    "reply": "string", // 后端从 provider 返回中提取的友好回复文本（best-effort）
    "raw": { /* provider 原始返回（可选） */ }
  }

行为与约束
- 身份：接口会尝试使用当前 `Principal` 的名称作为 `userKey` 进行速率限制；若无鉴权信息则使用客户端 IP。
- 速率限制：默认每个 `userKey` 每 `60` 秒允许 `20` 次请求，可通过配置覆盖（见下）。
- 输入审查：
  - 消息总长度上限：4000 字符（合并所有 message content）。
  - 拒绝包含明显危险或注入性片段的内容（如 `<script>`、`eval(`、`rm -rf`、`import os` 等）。
  - 不符合规则会返回 `400 Bad Request` 且 body 为 `{ "error": "..." }`。
- 限流返回：当超出速率限制时返回 `429 Too Many Requests`，body 为 `{ "error": "rate_limit_exceeded" }`。

配置（`application.properties`）
- `llm.provider.url`：上游 LLM 提供者地址（POST）。例如 OpenAI 的代理或自建代理 URL。
- `llm.provider.apikey`：上游 provider 的 API Key（可选，服务端将以 `Authorization: Bearer <key>` 传递）。强烈建议在后端配置并不要暴露给前端。
- `llm.rate.limit`：每窗口允许的请求数（默认 `20`）。
- `llm.rate.window.seconds`：速率限制窗口大小（秒，默认 `60`）。

示例（curl）

```
curl -X POST 'https://your-backend.example.com/api/llm' \
  -H 'Content-Type: application/json' \
  -d '{"messages":[{"role":"user","content":"请给我一个投资组合配置建议"}]}'
```

实现说明（后端行为要点）
- 代理请求（proxy）：后端将接收到的 JSON 原样转发到 `llm.provider.url`，并把上游响应的 `raw` 一并返回，同时尝试从上游响应中提取可读 `reply` 字段返回给前端。
- Mock 支持：当未配置 `llm.provider.url` 时，服务端会返回一个本地 mock 响应（用于前端本地调试）。
- 日志：建议在生产环境记录简短请求元数据（userKey、ip、status），但不要记录用户输入内容或上游返回中的敏感信息以防泄漏。

支持的 provider 类型
- `llm.provider.type` 支持：`openai`、`qianwen`、`deepseek`、`proxy`（默认 `proxy`）。
- 行为说明：
  - `openai`：后端会把请求构造成 OpenAI 的 `{"model":..., "messages": [...]}` 形式（会把 `options.model` 或 `llm.default.model` 注入到 `model` 字段）。
  - `qianwen`/`deepseek`：常见国内模型服务接收单体 `input`/`prompt` 字段，后端会把所有消息合并为一个字符串放入 `input`，并把 `model` 字段一并传递。
  - `proxy`：默认行为，直接把前端的 `LlmRequest` JSON 转发给 `llm.provider.url`，不做结构转换。

模型选择（如何在请求里指定）
- 前端可以在 `options` 中传入 `model` 字段：

  {
    "messages":[{"role":"user","content":"给我投资建议"}],
    "options": { "model": "gpt-4o-mini" }
  }

- 若未指定，后端会使用 `llm.default.model` 配置作为默认 model。

示例：OpenAI

```
curl -X POST 'http://localhost:8080/api/llm' \
  -H 'Content-Type: application/json' \
  -d '{"messages":[{"role":"user","content":"请给我一个投资组合配置建议"}], "options": {"model":"gpt-4o-mini"}}'
```

示例：千问/DeepSeek（示例 payload）

```
curl -X POST 'http://localhost:8080/api/llm' \
  -H 'Content-Type: application/json' \
  -d '{"messages":[{"role":"user","content":"请给我一个投资组合配置建议"}], "options": {"model":"qn-base"}}'
```

说明：对于 `qianwen` 或 `deepseek`，后端会把 `messages` 合并为 `input` 字段并转发到上游。

安全建议
- API Key：将第三方 LLM 的 API Key 保存在后端环境变量或配置文件中，仅服务端可读；前端不应保存或直接调用第三方 Key。
- 输入审查：后端需继续维护/扩充审查策略，阻止注入、恶意系统级指令、文件系统/网络命令等。
- 访问控制：将 `/api/llm` 放在需要登录的路由下（前端已在页面端限制 `USER` 与 `STAFF`），后端应再行验证用户会话并确认角色权限（视项目现有鉴权实现）。
- 计费与限额：在产品化场景下应结合用户级或组织级计费、配额管理，避免滥用导致高成本。

调试步骤
1. 若尚未配置上游 provider，为本地调试将 `llm.provider.url` 留空，后端返回 mock 回复。
2. 若使用真实 provider：在 `application.properties` 中设置 `llm.provider.url` 与 `llm.provider.apikey`，重启后端。
3. 使用前端或 curl 对 `/api/llm` 发送请求，检查后端日志以排查错误。

扩展建议
- 流式回答：可改为使用 SSE 或 HTTP stream（fetch streams）把上游的流式数据转发给前端，以获得更好体验。
- 后端代理安全中间件：增加敏感词检测、行为风控、黑名单/白名单、IP 限流等。
- 支持多 provider：实现 provider 适配层，根据 `options` 或租户配置选择不同 provider 并对返回做统一抽象。
