import { ConfigEnv, ServerOptions, UserConfig } from 'vite';
import { tanstackRouter } from '@tanstack/router-vite-plugin';
import mockDevServerPlugin from 'vite-plugin-mock-dev-server';
import react from '@vitejs/plugin-react';
import checker from 'vite-plugin-checker';
import svgr from 'vite-plugin-svgr';
import { startDemoApp, generateIntl, fetchVite } from './vite';



const server: ServerOptions = {
  open: true,
  port: 3000,
  proxy: {
    '/config': {
      target: 'http://localhost:8080',
      changeOrigin: false,
      secure: false,
    },
    '/userInfo': {
      target: 'http://localhost:8080',
      changeOrigin: false,
      secure: false,
    },
    '/groupsList': {
      target: 'http://localhost:8080',
      changeOrigin: false,
      secure: false,
    },
    '/worker/rest/api/': {
      target: 'http://localhost:8080',
      changeOrigin: false,
      secure: false,
    },
    '/dialob/': {
      target: 'http://localhost:9000',
      changeOrigin: false,
      secure: false,
    },
  }
}

// https://vitejs.dev/config/
export default function defineConfig(props: ConfigEnv): UserConfig {
  return {
    base: process.env.PUBLIC_URL || '',
    mode: 'development',
    plugins: [
      startDemoApp({ moduleName: '@dxs-ts/eveli-demo-app', server }),
      tanstackRouter({
        target: 'react',
        autoCodeSplitting: true,
        verboseFileRoutes: true,
        routesDirectory: './modules/eveli-routes/routes',
        generatedRouteTree: './modules/eveli-routes/routeTree.gen.ts'
      }),
      generateIntl({ ignoreErrors: true, intlDirectory: './modules/eveli-intl', csv: './eveli-ide/intl.csv' }),
      
      
      react({
        jsxImportSource: '@emotion/react',
        babel: { plugins: ['@emotion/babel-plugin'] },
      }),
      checker({
        typescript: {
          tsconfigPath: './modules/demo-app-eveli/tsconfig.json'
        }
      }),

      fetchVite({
        fetchDirectory: "./modules/eveli-api/fetch",
        fetchTreeDirectory: "./modules/eveli-api",
        fetchTreeGenFile: "./fetchTree.gen.ts",
        moduleName: "@dxs-ts/eveli-api"
      }),
      svgr({}),
      mockDevServerPlugin(),
    ],

    // material sometimes messes up variable loading order
    optimizeDeps: {
      include: ['@mui/material', '@mui/icons-material'],
      force: true,
      exclude: [
        'modules/demo-app-eveli/node_modules/.vite'
      ]
    },

  }
}
