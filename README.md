# 智能投顾系统

一个基于 Vue 3 + TypeScript + Element Plus 的现代化智能投资顾问系统前端。

## 🚀 功能特性

### 📊 仪表盘
- 实时投资概览
- 关键指标展示（总资产、日收益、总收益率、风险等级）
- 资产走势图表
- 资产配置饼图
- 持仓明细和最近交易记录

### 💼 投资组合
- 详细的持仓管理
- 交易记录追踪
- 资产配置分析
- 风险评估指标
- 快速操作功能（买入、卖出、重新平衡）

### 📈 市场分析
- 实时市场指数监控
- 市场趋势分析图表
- 行业板块分析
- 技术指标展示（RSI、MACD、KDJ等）
- 市场热点新闻
- 投资建议和风险提示

### 🤖 智能顾问
- AI驱动的投资建议
- 个性化资产配置建议
- 具体产品推荐
- 操作建议（买入/卖出）
- 风险评估雷达图
- 智能对话功能

### ⚙️ 系统设置
- 个人资料管理
- 投资偏好设置
- 安全设置
- 通知设置
- 显示设置
- 数据管理

## 🛠️ 技术栈

- **前端框架**: Vue 3
- **开发语言**: TypeScript
- **UI组件库**: Element Plus
- **图表库**: ECharts
- **状态管理**: Pinia
- **路由管理**: Vue Router
- **构建工具**: Vite
- **包管理器**: npm

## 📦 安装和运行

### 环境要求
- Node.js >= 16.0.0
- npm >= 8.0.0

### 安装依赖
```bash
npm install
```

### 开发环境运行
```bash
npm run dev
```

### 生产环境构建
```bash
npm run build
```

### 预览生产构建
```bash
npm run preview
```

## 📁 项目结构

```
src/
├── assets/          # 静态资源
├── components/      # 公共组件
├── router/          # 路由配置
├── stores/          # 状态管理
├── views/           # 页面组件
│   ├── DashboardView.vue    # 仪表盘
│   ├── PortfolioView.vue    # 投资组合
│   ├── AnalysisView.vue     # 市场分析
│   ├── AdvisorView.vue      # 智能顾问
│   └── SettingsView.vue     # 系统设置
├── App.vue          # 根组件
└── main.ts          # 入口文件
```

## 🎨 设计特色

### 现代化UI设计
- 采用深色侧边栏 + 浅色主内容区的经典布局
- 响应式设计，支持移动端适配
- 丰富的图表和可视化组件
- 流畅的动画和交互效果

### 用户体验优化
- 直观的导航结构
- 清晰的信息层级
- 便捷的操作流程
- 实时的数据反馈

### 数据可视化
- 多种图表类型（折线图、饼图、雷达图、柱状图）
- 交互式图表组件
- 实时数据更新
- 自定义图表配置

## 🔧 配置说明

### Element Plus 配置
- 使用中文语言包
- 自定义主题色彩
- 全局图标注册

### ECharts 配置
- 按需引入图表组件
- 自定义图表主题
- 响应式图表适配

### 路由配置
- 基于文件的路由结构
- 路由守卫配置
- 懒加载优化

## 📱 响应式支持

系统支持多种设备尺寸：
- 桌面端（>= 1200px）
- 平板端（768px - 1199px）
- 移动端（< 768px）

## 🚀 部署说明

### 开发环境
1. 克隆项目
2. 安装依赖：`npm install`
3. 启动开发服务器：`npm run dev`
4. 访问：`http://localhost:5173`

### 生产环境
1. 构建项目：`npm run build`
2. 部署 `dist` 目录到 Web 服务器
3. 配置服务器支持 SPA 路由

## 🤝 贡献指南

1. Fork 项目
2. 创建功能分支：`git checkout -b feature/AmazingFeature`
3. 提交更改：`git commit -m 'Add some AmazingFeature'`
4. 推送分支：`git push origin feature/AmazingFeature`
5. 提交 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 📞 联系方式

如有问题或建议，请通过以下方式联系：
- 邮箱：your-email@example.com
- 项目地址：https://github.com/your-username/investment-advisor

## 🙏 致谢

感谢以下开源项目的支持：
- [Vue.js](https://vuejs.org/)
- [Element Plus](https://element-plus.org/)
- [ECharts](https://echarts.apache.org/)
- [Vite](https://vitejs.dev/)

