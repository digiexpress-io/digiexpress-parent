import { ModuleRegistry } from "../module-registry";

export declare namespace Command_ResolveDependencyTree {
  export interface Input {
    registry: ModuleRegistry;
    targetModule?: string;
  }

  export interface Result {
    modulesToValidate: string[];
  }
}

export class Command_ResolveDependencyTree {
  execute(input: Command_ResolveDependencyTree.Input): Command_ResolveDependencyTree.Result {
    const { registry, targetModule } = input;

    // Determine validation scope
    const modulesToValidate = targetModule
      ? _getDependencyTree(registry, targetModule)
      : Object.keys(registry.modules);

    console.log(`📊 Validating ${modulesToValidate.length} modules in scope`);

    return { modulesToValidate };
  }
}

// Pure transformative functions
function _getDependencyTree(registry: ModuleRegistry, targetModule: string): string[] {
  try {
    const visited = new Set<string>();
    const result: string[] = [];

    const traverse = (moduleName: string) => {
      if (visited.has(moduleName)) return;
      if (!registry.modules[moduleName]) {
        console.warn(`⚠️  Module '${moduleName}' referenced but not found in registry`);
        return;
      }

      visited.add(moduleName);
      result.push(moduleName);

      // Traverse internal dependencies
      const moduleInfo = registry.modules[moduleName];
      const internalDeps = moduleInfo.internalDependencies || [];

      for (const dep of internalDeps) {
        traverse(dep);
      }
    };

    traverse(targetModule);
    console.log(`🌳 Dependency tree for ${targetModule}: ${result.length} modules`);
    return result;

  } catch (error) {
    throw new Error(`💥 DEPENDENCY TREE CORRUPTION: Failed to traverse dependencies for '${targetModule}': ${error.message}`);
  }
}