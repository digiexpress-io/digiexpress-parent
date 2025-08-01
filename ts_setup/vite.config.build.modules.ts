import { ConfigEnv, UserConfig } from 'vite';
import { moduleRegistryCreate } from './vite'




// https://vitejs.dev/config/
export default function defineConfig(props: ConfigEnv): UserConfig {
  return {
    plugins: [
      moduleRegistryCreate(),
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