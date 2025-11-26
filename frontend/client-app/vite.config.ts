import { fileURLToPath, URL } from 'node:url'

import { defineConfig, type ProxyOptions } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// 后端服务地址配置
const factorService = 'http://localhost:8080'      // 因子服务
const portfolioService = 'http://localhost:8081'   // 组合服务
const strategyService = 'http://localhost:8082'    // 策略服务

// 代理配置 - 按照后端API路径规范进行路由
// ⚠️ 注意：更长的路径必须放在前面，避免被短路径匹配
const proxy: Record<string, string | ProxyOptions> = {
  // ========== strategy-backend (8082) - /api/factors 必须在 /api/factor 前面 ==========
  '/api/factors': {
    target: strategyService,
    changeOrigin: true,
    secure: false
  },

  // ========== factor-backend (8080) ==========
  '/api/factor': {
    target: factorService,
    changeOrigin: true,
    secure: false
  },

  // ========== portfolio-backend (8081) ==========
  '/api/auth': {
    target: portfolioService,
    changeOrigin: true,
    secure: false
  },
  '/api/funds': {
    target: portfolioService,
    changeOrigin: true,
    secure: false
  },
  '/api/subscription': {
    target: portfolioService,
    changeOrigin: true,
    secure: false
  },
  '/api/strategy-combination': {
    target: portfolioService,
    changeOrigin: true,
    secure: false
  },
  '/api/strategies': {
    target: portfolioService,
    changeOrigin: true,
    secure: false
  },
  '/api/investor': {
    target: portfolioService,
    changeOrigin: true,
    secure: false
  },
  '/api/risk-assessment': {
    target: portfolioService,
    changeOrigin: true,
    secure: false
  },
  '/api/agreements': {
    target: portfolioService,
    changeOrigin: true,
    secure: false
  },
  '/api/llm': {
    target: portfolioService,
    changeOrigin: true,
    secure: false
  },
  '/combos': {
    target: portfolioService,
    changeOrigin: true,
    secure: false
  },

  // ========== strategy-backend (8082) ==========
  '/api/strategy-management': {
    target: strategyService,
    changeOrigin: true,
    secure: false
  },
  '/api/strategy-monitoring': {
    target: strategyService,
    changeOrigin: true,
    secure: false
  },
  '/api/strategy-rebalance': {
    target: strategyService,
    changeOrigin: true,
    secure: false
  },
  '/api/strategy-warning': {
    target: strategyService,
    changeOrigin: true,
    secure: false
  },
  '/api/python-data': {
    target: strategyService,
    changeOrigin: true,
    secure: false
  }
}

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools()
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    proxy
  }
})
