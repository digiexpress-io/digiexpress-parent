import { ModuleRegistry, ModuleInfo } from '../module-registry'

export declare namespace Command_ResolveTargetModule {
  export interface Input {
    registry: ModuleRegistry;
    moduleName: string;
  }

  export interface Result {
    targetModuleInfo: ModuleInfo;
  }
}

export class Command_ResolveTargetModule {
  execute(input: Command_ResolveTargetModule.Input): Command_ResolveTargetModule.Result {
    const { registry, moduleName } = input;

    // Find target module in registry
    if (!registry.modules[moduleName]) {
      const availableModules = Object.keys(registry.modules);
      throw new Error(
        `💥 TARGET MODULE NOT FOUND: Module '${moduleName}' not found in registry\n` +
        `🔧 Available modules:\n${availableModules.map(name => `   - ${name}`).join('\n')}`
      );
    }

    const targetModuleInfo = registry.modules[moduleName];
    console.log(`✅ Target module resolved: ${targetModuleInfo.name}`);

    return { targetModuleInfo };
  }
}