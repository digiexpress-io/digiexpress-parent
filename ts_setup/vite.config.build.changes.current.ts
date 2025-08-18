import { ConfigEnv, UserConfig } from 'vite';
import { buildChanges } from './vite'


// https://vitejs.dev/config/
export default function defineConfig(props: ConfigEnv): UserConfig {
  return {
    plugins: [
      buildChanges({

      })
    ],
    build: {
      lib: {
        entry: 'vite/dummy.js',
        name: 'dummy',
        fileName: (format) => `dummy.js`
      },
      rollupOptions: {
        external: ['es'],
      }
    }
  }
}