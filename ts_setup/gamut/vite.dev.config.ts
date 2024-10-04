import { ConfigEnv, UserConfig } from 'vite';
import { TanStackRouterVite } from '@tanstack/router-vite-plugin';

import react from '@vitejs/plugin-react';
import checker from 'vite-plugin-checker';
import svgr from 'vite-plugin-svgr';
import { fileURLToPath, URL } from 'url';

// rollup module delegate
const alias: { find: string, replacement: string }[] = [
  { find: '@dxs-ts/gamut', replacement: fileURLToPath(new URL('./src', import.meta.url)) },

];

// https://vitejs.dev/config/
export default function defineConfig(props: ConfigEnv): UserConfig {
  return {
    base: process.env.PUBLIC_URL || '',
    plugins: [
      react({
        jsxImportSource: '@emotion/react',
        babel: {
          plugins: ['@emotion/babel-plugin'],
        },
      }),
      TanStackRouterVite(),
      checker({ typescript: true }),
      svgr({
        svgrOptions: {
          // svgr options
        },
      }),
    ],
    build: {
      chunkSizeWarningLimit: 5000,
      outDir: './build',
      assetsDir: 'static'
    },
    resolve: { alias },
    server: {
      open: true,
      port: 3000,
    },
    optimizeDeps: {
      //https://github.com/vitejs/vite/issues/12423
      //https://github.com/mui/material-ui/issues/32727
      include: [
        '@mui/material/CssBaseline', 
        '@mui/material/Box', 

      ],
      force: true
    },
    define: {
      // react redux error
      'process.env.IS_SSR': undefined,
      'process.env.REACT_APP_LOCAL_DEV_MODE': true + '',
    },
  }
}