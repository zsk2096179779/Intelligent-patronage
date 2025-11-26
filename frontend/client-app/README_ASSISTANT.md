# 智能投顾（LLM）模块说明

简介：该模块为前端集成的智能投顾助手，前端通过 `/api/llm` 向后端发起请求，后端负责代理到具体 LLM（例如 OpenAI、Azure、或者本地模型）。

新增文件：
- `src/utils/llm.ts`：LLM 请求封装与 prompt 构建
- `src/stores/assistant.ts`：Pinia 状态管理（会话、设置、发送消息）
- `src/components/Assistant/AssistantPanel.vue`：主交互面板（输入、设置、操作）
- `src/components/Assistant/AssistantChat.vue`：消息列表展示
- `src/views/AssistantView.vue`：页面级视图

路由：已新增 `/assistant` 路由，登录后 `USER` 和 `STAFF` 可访问。

后端约定：
- 前端向 `POST /api/llm` 发送 JSON：{ messages: [{role, content}], options?: {} }
- 后端返回 JSON：{ reply: string, raw?: any }

环境与安全：
- 推荐将 LLM API Key 保存在后端，不要暴露于前端。
- 后端应对用户输入进行审查与速率限制，避免滥用计费或模型误用。

如何调试：
1. 启动后端代理（或使用 mock）以响应 `/api/llm`。
2. 前端运行：
```powershell
pnpm install
pnpm dev
```

如果你需要我继续：
- 我可以增加流式回答支持（sse 或 fetch streams），或
- 编写后端示例（Node/Express）作为 LLM 代理。
