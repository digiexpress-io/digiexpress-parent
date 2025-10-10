import type { Plugin, ServerOptions, UserConfig } from 'vite';


import { ModuleRegistryCacheBuilder, } from '../module-registry-cache';
import { ModuleInfo, ModuleRegistry } from '../module-registry';
import path from 'node:path';
import { TSConfigBuilder, TSConfigOutput } from '../gen-tsconfig';



export function startDemoApp(options: {
  moduleName: string,
  server?: ServerOptions
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

      const allTsConfigs = new TSConfigBuilder({ rootPath }).build(registry, true);
      tsConfig = allTsConfigs.find(e => e.moduleName === options.moduleName)?.tsConfig!;

      if (!tsConfig) {
        throw new Error(`❌ Can't resolve aliases for name: ${options.moduleName}!`);
      }

      const alias = _getAliases(rootPath, registry, allTsConfigs, module);
      return {
        root: path.join(rootPath, module.path),
        build: {
          chunkSizeWarningLimit: 5000,
          outDir: './build/'+ options.moduleName,
          assetsDir: 'static/'+options.moduleName,
        },
        resolve: { alias },
        optimizeDeps: {
          //https://github.com/vitejs/vite/issues/12423
          //https://github.com/mui/material-ui/issues/32727
          include: [
            '@mui/material/CssBaseline',
            '@mui/material/Box',
            '@mui/material/SvgIcon',
            '@mui/material', 
            '@mui/icons-material',
            '@tanstack/react-router'
          ],
          force: true,
      
      
          exclude: [
            'modules/demo-app-eveli/node_modules/.vite',
            'modules/demo-app-eveli/node_modules/.vite/*'
          ]

        },
        define: {
          // react redux error
          'process.env.IS_SSR': undefined,
          'process.env.REACT_APP_LOCAL_DEV_MODE': true + '',
        },
        ...(options.server ? { server: options.server } : {})
      }
    },

  };
}

interface Alias { find: string, replacement: string };

function _getAliases(
  rootPath: string, 
  registry: ModuleRegistry,
  allTsConfig: {
    moduleName: string;
    outputPath: string;
    tsConfig: TSConfigOutput;
  }[],
  module: ModuleInfo
): Alias[] {


  const result: Alias[] = [];
  const modulesForAliases: string[] = [];
  for (const moduleName of module.actualInternalDependencies) {
    _getAliasTree(registry, registry.modules[moduleName], modulesForAliases);
  }

  console.log(modulesForAliases);

  for (const moduleName of modulesForAliases) {
    const module = registry.modules[moduleName];
    /*
    const tsConfig = allTsConfig.find(e => e.moduleName === moduleName)?.tsConfig!;

    if(!tsConfig._generated.alias) {
      continue;
    }

    const resolved = tsConfig._generated.alias![moduleName];
    if(!resolved) {
      continue;
    }
    const [src] = resolved;
    */
    
    const alias = {
      find: moduleName,
      replacement: path.resolve(rootPath, module.path, 'index.ts')
    };
    result.push(alias);
  }

  return result;
}

function _stripOneLevelUp(src: string) {
  if (src.startsWith("../")) {
    return src.substring(3);
  }
  return src;
}

function _getAliasTree(registry: ModuleRegistry, module: ModuleInfo, collector: string[] = []): string[] {
  if (!collector.includes(module.name)) {
    collector.push(module.name);
  }
  for (const moduleName of module.actualInternalDependencies) {
    const next = registry.modules[moduleName];
    if (!collector.includes(next.name)) {
      _getAliasTree(registry, next, collector)
    }
  }

  return collector;
}