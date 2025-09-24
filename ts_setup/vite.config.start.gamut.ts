import { ConfigEnv, ServerOptions, UserConfig } from 'vite';
import { tanstackRouter } from '@tanstack/router-vite-plugin';

import react from '@vitejs/plugin-react';
import checker from 'vite-plugin-checker';
import svgr from 'vite-plugin-svgr';
import { startDemoApp, generateIntl } from './vite';


const server: ServerOptions =  {
  open: true,
  port: 3001,
  proxy: {
    '/portal': {
      target: 'http://localhost:8080',
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
      startDemoApp({ moduleName: '@dxs-ts/gamut-demo-app', server }),
      tanstackRouter({
        target: 'react',
        autoCodeSplitting: true,
        verboseFileRoutes: true,
        routesDirectory: './modules/gamut-routes/routes',
        generatedRouteTree: './modules/gamut-routes/routeTree.gen.ts'
      }),
      generateIntl({ ignoreErrors: true, intlDirectory: './modules/gamut-intl', csv: './gamut/intl.csv' }),
      react({
        jsxImportSource: '@emotion/react',
        babel: { plugins: ['@emotion/babel-plugin'] },
      }),
      checker({ 
        typescript: {
          tsconfigPath: './modules/demo-app-gamut/tsconfig.json'
        } 
      }),
      svgr({}),
    ],
  }
}
