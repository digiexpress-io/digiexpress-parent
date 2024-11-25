import { ConfigEnv, UserConfig } from 'vite';
import mockDevServerPlugin from 'vite-plugin-mock-dev-server'
import react from '@vitejs/plugin-react';
import checker from 'vite-plugin-checker';
import svgr from 'vite-plugin-svgr';

import { alias } from './vite.paths.config';


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
      checker({ typescript: true }),
      svgr({
        svgrOptions: {
          // svgr options
        },
      }), 
      mockDevServerPlugin()
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

        '/login-gatway/worker': {
          target: 'http://localhost:8080',
          changeOrigin: false,
          secure: false,
        },
      }
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
      'process.env.REACT_APP_START_MODE': '"frontdesk"',

      'process.env.VITE_IAP_REFRESH': true + '',
      'process.env.VITE_HOST_URL': '"http://localhost:3000"',
      'process.env.VITE_ENV_TYPE': '"test"'
    },
  }
}