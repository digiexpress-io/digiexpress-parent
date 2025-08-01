import { Plugin } from 'vite'
import { ModuleRegistryCacheBuilder } from '../module-registry-cache';
import { ESLintConfigBuilder } from '../gen-eslint'
import { TSConfigBuilder } from '../gen-tsconfig'


export function moduleRegistryCreate(): Plugin {
  return {
    name: 'module-registry-create',

    config() {
      const rootPath = process.cwd();
      new ModuleRegistryCacheBuilder({
          onRegistryBuilt(registry) {
            new TSConfigBuilder().build(registry);
            new ESLintConfigBuilder().build(registry);
          },
        }).build(rootPath, true);
    }

  };
}


