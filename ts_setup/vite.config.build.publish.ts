import { ConfigEnv, UserConfig } from 'vite';
import { releaseAllModules } from './vite'




// https://vitejs.dev/config/
export default function defineConfig(props: ConfigEnv): UserConfig {
  return {
    mode: 'production',
    base: process.env.PUBLIC_URL || '',
    plugins: [
      releaseAllModules({
        dryRun: true,
        skipValidation: false,
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