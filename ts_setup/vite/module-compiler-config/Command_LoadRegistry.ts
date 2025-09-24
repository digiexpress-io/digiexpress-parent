import { existsSync, readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import { ModuleRegistry } from '../module-registry'
import { ModuleRegistryCacheBuilder } from '../module-registry-cache';

export declare namespace Command_LoadRegistry {
  export interface Input {
    rootPath: string;
    registryPath: string;
    registryRecreate: boolean;
  }

  export interface Result {
    registry: ModuleRegistry;
  }
}

export class Command_LoadRegistry {
  execute(input: Command_LoadRegistry.Input): Command_LoadRegistry.Result {
    const { rootPath, registryPath } = input;

    console.log(`🏗️  Loading registry from: ${registryPath}`);

    const fullRegistryPath = resolve(rootPath, registryPath);

    if (!existsSync(fullRegistryPath) || input.registryRecreate) {
      /*
      throw new Error(
        `💥 REGISTRY MISSING: Cannot find module registry at '${registryPath}'\n` +
        `🔧 SOLUTION: Run the module registry plugin first to generate the registry.\n` +
        `   Add moduleRegistryPlugin() to your vite config before libBuildPlugin()`
      );*/
      new ModuleRegistryCacheBuilder({ registryPath }).build(rootPath, true);
    }

    const registryContent = readFileSync(fullRegistryPath, 'utf-8');
    const registry = JSON.parse(registryContent);

    console.log(`✅ Registry loaded: ${Object.keys(registry.modules).length} modules`);

    return { registry };
  }
}