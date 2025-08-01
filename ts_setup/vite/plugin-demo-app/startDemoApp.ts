import type { Plugin, UserConfig } from 'vite';
import { fileURLToPath } from 'node:url';

import { ModuleRegistryCacheBuilder, } from '../module-registry-cache';
import { ModuleInfo, ModuleRegistry } from '../module-registry';
import path from 'node:path';
import { TSConfigBuilder, TSConfigOutput } from '../gen-tsconfig';


/*
const alias: { find: string, replacement: string }[] = [
  { find: '@dxs-ts/gamut', replacement: fileURLToPath(new URL('./src', import.meta.url)) },
];
*/


export function startDemoApp(options: {
  moduleName: string
}): Plugin {

  const builder = new ModuleRegistryCacheBuilder({});
  let tsConfig: TSConfigOutput;
  let registry: ModuleRegistry;

  return {
    name: 'start-demo-app',
    enforce: 'pre',

    config(config): UserConfig {
      const rootPath = process.cwd();
      registry = builder.build(rootPath);

      const module = registry.modules[options.moduleName];
      if (!module) {
        throw new Error(`❌ Can't find module with name: ${options.moduleName}!`);
      }

      tsConfig = new TSConfigBuilder({}).build(registry, true);
      if (!tsConfig) {
        throw new Error(`❌ Can't resolve aliases for name: ${options.moduleName}!`);
      }

      const alias = _getAliases(rootPath, registry, tsConfig, module);      
      return {
        root: path.join(rootPath, module.path),
        build: {
          chunkSizeWarningLimit: 5000,
          outDir: './build/',
          assetsDir: 'static/',
        },
        resolve: { alias },
        optimizeDeps: {
          //https://github.com/vitejs/vite/issues/12423
          //https://github.com/mui/material-ui/issues/32727
          include: [
            '@mui/material/CssBaseline',
            '@mui/material/Box',
          ],
          force: true
        },
        define: {
          // react redux error
          'process.env.IS_SSR': undefined,
          'process.env.REACT_APP_LOCAL_DEV_MODE': true + '',
        },
      }
    },

  };
}

interface Alias { find: string, replacement: string };

function _getAliases(rootPath: string, registry: ModuleRegistry, tsConfig: TSConfigOutput, module: ModuleInfo): Alias[] {
  const result: Alias[] = [];
  const modulesForAliases: string[] = [];
  for (const moduleName of module.actualInternalDependencies) {
    _getAliasTree(registry, registry.modules[moduleName], modulesForAliases);
  }

  for(const moduleName of modulesForAliases) {
    const [src] = tsConfig.compilerOptions.paths[moduleName];
    const alias = { 
      find: moduleName, 
      replacement:  path.resolve(rootPath, src)
    };
    result.push(alias);
  }
  return result;
}


function _getAliasTree(registry: ModuleRegistry, module: ModuleInfo, collector: string[] = []): string[] {
  if(!collector.includes(module.name)) {
    collector.push(module.name);
  }
  for (const moduleName of module.actualInternalDependencies) {
    const next = registry.modules[moduleName];
    if(!collector.includes(next.name)) {
      _getAliasTree(registry, next, collector)
    }
  }

  return collector;
}