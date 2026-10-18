import { defineConfig, loadEnv } from 'vite'
import path from 'path'
import createVitePlugins from './vite/plugins'

const baseUrl = 'http://localhost:8081' // 后端接口

// https://vitejs.dev/config/
export default defineConfig(({ mode, command }) => {
  const env = loadEnv(mode, process.cwd())
  const { VITE_APP_ENV } = env
  return {
    // 部署生产环境和开发环境下的URL。
    // 默认情况下，vite 会假设你的应用是被部署在一个域名的根路径上
    // 例如 https://www.ruoyi.vip/。如果应用被部署在一个子路径上，你就需要用这个选项指定这个子路径。例如，如果你的应用被部署在 https://www.ruoyi.vip/admin/，则设置 baseUrl 为 /admin/。
    base: VITE_APP_ENV === 'production' ? '/' : '/',
    // 依赖预构建缓存放在工程内，避免与模板工程共用 node_modules 时互相干扰。
    // 注意：不要在这里加 optimizeDeps.include —— 一旦改动它，vite 会重建 deps 缓存并删除旧临时目录，
    // 本机安全删除策略会拦截批量删除（>50 文件的 rm），导致预构建失败、首屏白屏。
    cacheDir: '.vite-cache',
    plugins: createVitePlugins(env, command === 'build'),
    resolve: {
      // https://cn.vitejs.dev/config/#resolve-alias
      alias: {
        // 设置路径
        '~': path.resolve(__dirname, './'),
        // 设置别名
        '@': path.resolve(__dirname, './src')
      },
      // https://cn.vitejs.dev/config/#resolve-extensions
      extensions: ['.mjs', '.js', '.ts', '.jsx', '.tsx', '.json', '.vue']
    },
    // 打包配置
    build: {
      // https://vite.dev/config/build-options.html
      sourcemap: command === 'build' ? false : 'inline',
      outDir: 'dist',
      assetsDir: 'assets',
      chunkSizeWarningLimit: 2000,
      rollupOptions: {
        output: {
          chunkFileNames: 'static/js/[name]-[hash].js',
          entryFileNames: 'static/js/[name]-[hash].js',
          assetFileNames: 'static/[ext]/[name]-[hash].[ext]'
        }
      }
    },
    // vite 相关配置
    server: {
      port: 81,
      // 端口被占时直接报错，不要悄悄换成 82/83 —— 否则浏览器和代理会连到不同实例，表现为页面空白
      strictPort: true,
      host: true,
      open: true,
      // 启动时预转换首屏模块，缓解首次打开时的长时间白屏
      warmup: {
        clientFiles: [
          './src/main.js',
          './src/App.vue',
          './src/permission.js',
          './src/router/index.js',
          './src/store/modules/*.js',
          './src/utils/*.js',
          './src/layout/index.vue',
          './src/layout/components/**/*.vue',
          './src/components/**/*.vue',
          './src/views/login.vue',
          './src/views/index.vue',
          './src/views/kids/dashboard/index.vue'
        ]
      },
      proxy: {
        // https://cn.vitejs.dev/config/#server-proxy
        '/dev-api': {
          target: baseUrl,
          changeOrigin: true,
          rewrite: (p) => p.replace(/^\/dev-api/, '')
        },
         // springdoc proxy
         '^/v3/api-docs/(.*)': {
          target: baseUrl,
          changeOrigin: true,
        }
      }
    },
    css: {
      postcss: {
        plugins: [
          {
            postcssPlugin: 'internal:charset-removal',
            AtRule: {
              charset: (atRule) => {
                if (atRule.name === 'charset') {
                  atRule.remove()
                }
              }
            }
          }
        ]
      }
    }
  }
})
