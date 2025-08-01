
import react from '@vitejs/plugin-react';
import checker from 'vite-plugin-checker';
import svgr from 'vite-plugin-svgr';
import dts from 'unplugin-dts/vite'

import { ConfigEnv, UserConfig } from 'vite';
import { DTSBuilder, buildOneModule } from './vite'




// https://vitejs.dev/config/
export default function defineConfig(props: ConfigEnv): UserConfig {

  const moduleName = process.env.UNI_BUILD_MODULE;
  if(!moduleName) {
    throw new Error("UNI_BUILD_MODULE must be defined!");
  }

  const registryRecreate = !!process.env.UNI_BUILD_FORCE;

  return {
    mode: 'production',
    base: process.env.PUBLIC_URL || '',
    plugins: [
      react({}),

      dts(new DTSBuilder({
        tsconfigPath: './tsconfig.json',
        bundleTypes: true,
        processImports: true
      }).build()),

      svgr({ svgrOptions: {} }),
      checker({ typescript: true }),

      buildOneModule({
        moduleName: moduleName!, // @dxs-ts/gamut   // MUST match package.json
        strictValidation: true,  // No mercy mode
        registryRecreate
      }),
    ],
  }
}
