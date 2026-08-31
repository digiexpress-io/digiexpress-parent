
import react from '@vitejs/plugin-react';
import checker from 'vite-plugin-checker';
import svgr from 'vite-plugin-svgr';
import dts from 'unplugin-dts/vite';
import { visualizer } from 'rollup-plugin-visualizer';

import { ConfigEnv, UserConfig } from 'vite';
import { DTSBuilder, buildOneModule, ModuleRegistryCacheBuilder } from './vite'



// https://vitejs.dev/config/
export default function defineConfig(props: ConfigEnv): UserConfig {

  const moduleName = process.env.UNI_BUILD_MODULE;
  if (!moduleName) {
    throw new Error("UNI_BUILD_MODULE must be defined!");
  }

  const registryRecreate = !!process.env.UNI_BUILD_FORCE;

  const rootPath = process.cwd();
  const registry = new ModuleRegistryCacheBuilder().build(rootPath, false);
  const module = registry.modules[moduleName!];

  return {
    mode: 'production',
    base: process.env.PUBLIC_URL || '',
    plugins: [

      react({}),

      svgr({ svgrOptions: {} }),
      checker({
        typescript: {
          tsconfigPath: "./" + module.path + '/tsconfig.json'
        }
      }),
      buildOneModule({
        moduleName: moduleName!, // @dxs-ts/gamut   // MUST match package.json
        strictValidation: true,  // No mercy mode
        registryRecreate
      }),
      dts({
        ...new DTSBuilder({
          registry,
          moduleName: moduleName!,
          bundleTypes: true,
          processImports: true
        }).build(),
      }),
      /*
      visualizer({
        filename: './dist/stats.html',
        gzipSize: true,
        brotliSize: true,
        open: true, // auto-opens the treemap in your browser after build
      }),
      */
    ],
    
    esbuild: {
      jsx: 'automatic',
      define: {
        'process.env.NODE_ENV': '"production"'
      }
    },
  }
}
