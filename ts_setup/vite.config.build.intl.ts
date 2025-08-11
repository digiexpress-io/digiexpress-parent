import { ConfigEnv, UserConfig } from 'vite';
import { buildOneModuleIntl } from './vite';


// https://vitejs.dev/config/
export default function defineConfig(props: ConfigEnv): UserConfig {

  const moduleName = process.env.INTL_MODULES;
  if (!moduleName) {
    throw new Error("INTL_MODULES must be defined!");
  }

  const inputCsv = process.env.INTL_FILE;
  if (!inputCsv) {
    throw new Error("INTL_FILE must be defined!");
  }

  return {
    mode: 'production',
    base: process.env.PUBLIC_URL || '',
    plugins: [
      buildOneModuleIntl({
        moduleName: moduleName!, // @dxs-ts/gamut   // MUST match package.json
        inputCsv: inputCsv,
        outputCsv: inputCsv.replace('.csv', '_cleaned.csv')
      })
    ],
    build: {
      lib: {
        entry: 'vite/dummy.js',
        name: 'dummy',
        fileName: (_format) => `dummy.js`
      },
      rollupOptions: {
        external: ['es'],
      }
    }
  }
}
