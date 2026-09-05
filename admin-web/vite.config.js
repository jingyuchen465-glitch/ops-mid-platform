import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    host: '127.0.0.1',
    // 本机 4416-5340 端口段被 Hyper-V/WSL 保留，绑定即 EACCES，
    // 5173/5172 均落在此段内，改用 8000（不在保留段）
    port: 8000,
    proxy: { '/api': 'http://localhost:8090' }
  }
})
