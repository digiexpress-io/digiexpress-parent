import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';
import * as path from 'path';
import checker from 'vite-plugin-checker';

const modules: string[] = [
  'components-burger',
  'components-colors',
  'components-customer',
  'components-dialob',
  'components-generic',
  'components-hdes',
  'components-project',
  'components-stencil',
  'components-asset-mgmt',
  'components-release-mgmt',
  'components-task',
  'components-dialob',
  'components-dialob-composer',
  'components-access-mgmt',
  'components-org-chart',
  'components-user-profile',
  'components-libra',
  'components-flyout-menu',
  'components-xtable',
  'components-xfile-system',


  'descriptor-events',
  'descriptor-customer',
  'descriptor-organization',
  'descriptor-project',
  'descriptor-task',
  'descriptor-avatar',
  'descriptor-grouping',
  'descriptor-tabbing',
  'descriptor-prefs',
  'descriptor-dialob',
  'descriptor-access-mgmt',
  'descriptor-popper',
  'descriptor-sys-config',
  'descriptor-libra',
  'descriptor-backend',


  'context',
  'table',
  'logger',

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
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')

  let devMode = false;
  try {
    if (env.REACT_APP_LOCAL_DEV_MODE) {
      console.log("running dev");
      devMode = true;
    }
  } catch (e) {
    // nothing to do
  }
  return {
    base: process.env.PUBLIC_URL || '',
    plugins: [react(), checker({ typescript: true })],
    build: { chunkSizeWarningLimit: 5000 },
    resolve: { alias },

    define: {
      // react redux error
      "process.env.IS_SSR": undefined,
      "process.env.REACT_APP_LOCAL_DEV_MODE": devMode,
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
  }
}
);