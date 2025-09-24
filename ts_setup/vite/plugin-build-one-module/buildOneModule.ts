import type { Plugin, UserConfig } from 'vite';

import { ModuleCompilerConfig, ModuleCompilerConfigBuilder, ModuleCompilerConfigWriter, ModuleCompilerOptions } from '../module-compiler-config';


export function buildOneModule(user_options: ModuleCompilerOptions & { 
  registryRecreate: boolean,
  
}): Plugin {
  
  const builder = new ModuleCompilerConfigBuilder(user_options);
  let built: ModuleCompilerConfig;  

  return {
    name: 'build-one-module',
    enforce: 'pre',

    config(config): UserConfig {
      built = builder.build(user_options.registryRecreate);
      return {
        resolve: built.resolve,
        build: built.build,
        mode: 'production',
        esbuild: {
          jsx: 'automatic',
          define: {
            'process.env.NODE_ENV': '"production"'
          }
        }
      }
    },
    closeBundle(error) {
      if(error) {

      } else if(built) {
        new ModuleCompilerConfigWriter(built).build();
      }
    }
  };
}
