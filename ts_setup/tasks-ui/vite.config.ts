import { Plugin, defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import * as path from 'path';


const modules: string[] = [
  'components-burger',
  'components-colors',
  'components-customer',
  'components-dialob',
  'components-generic',
  'components-hdes',
  'components-project',
  'components-stencil',
  'components-sys-config',
  'components-task',
  'components-tenant',
  'components-user-profile',

  'descriptor-customer',
  'descriptor-organization',
  'descriptor-project',
  'descriptor-task',
  'descriptor-tenant',
  'descriptor-tenant-config',
  'descriptor-user-profile',

  'client',
  'context',
  'table',

  'app-frontoffice',
  'app-hdes',
  'application',
  'app-projects',
  'app-stencil',
  'app-tasks',
  'app-tenant',
]

// rollup module delegate
const alias: { find: string, replacement: string }[] = modules.map(mod => ({ find: mod, replacement: `/src/${mod}` }));


// https://vitejs.dev/config/
export default defineConfig({
  base: process.env.PUBLIC_URL || '',
  plugins: [react()],
  build: { chunkSizeWarningLimit: 5000 },
  resolve: { alias },

 
  define: {
    // react redux error
    "process.env.IS_SSR": undefined,
  },

  optimizeDeps: {
    esbuildOptions: {
      plugins: [
        {
          // https://github.com/bvaughn/react-virtualized/issues/1212
          name: 'resolve-fixup',
          setup(build) {
            build.onResolve({ filter: /react-virtualized/ }, async () => {
              return {
                path: path.resolve('./node_modules/react-virtualized/dist/umd/react-virtualized.js'),
              }
            })
          },
        }
      ]
    }
  }
})