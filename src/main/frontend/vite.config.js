import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
    plugins: [react()],
    server: {
        proxy: {
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true,
                // secure: false, // https를 사용하는 경우 인증서 무시 설정이 필요할 수 있습니다.
                // rewrite: (path) => path.replace(/^\/api/, ''), // 백엔드에서 /api 접두사를 쓰지 않는다면 이 줄의 주석을 푸세요.
            },
        },
    },
})