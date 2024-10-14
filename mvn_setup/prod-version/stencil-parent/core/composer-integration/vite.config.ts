import { defineConfig } from 'vite';
import dts from 'vite-plugin-dts'
import peerDepsExternal from 'rollup-plugin-peer-deps-external'

import react from '@vitejs/plugin-react';
import checker from 'vite-plugin-checker';
import svgr from 'vite-plugin-svgr';
import { resolve } from 'path';

// https://vitejs.dev/config/
export default defineConfig((props) => {
  const { command } = props;

  return {
    base: process.env.PUBLIC_URL || '',
    resolve: { alias: [] },
    plugins: [
      react({}),
      dts({ rollupTypes: true }),
      checker({ typescript: true }),
      svgr({ svgrOptions: {} }),
    ],

    build: {
      outDir: 'build',
      lib: {
        // Could also be a dictionary or array of multiple entry points
        entry: resolve(__dirname, 'src/index.ts'),
        name: '@dxs-ts/eveli-ide-integration',
        // the proper extensions will be added
        fileName: 'index',
        formats: ['es']
      },
      rollupOptions: {
        plugins: [peerDepsExternal()],
      }
    },
    
    server: {
      proxy: {
        '/rest': 'http://localhost:8080'
      }
    },
    define: {
      // react redux error
      'process.env.IS_SSR': undefined,
      'process.env.REACT_APP_LOCAL_DEV_MODE': false + '',
    },
  }
});