import { ModuleRegistry } from '../module-registry';

export declare namespace Command_ValidateRegistryStructure {
  export interface Input {
    registry: ModuleRegistry;
    targetModule?: string;
  }

  export interface Result {
    // void - throws on corruption, returns normally if valid
  }
}

export class Command_ValidateRegistryStructure {
  execute(input: Command_ValidateRegistryStructure.Input): Command_ValidateRegistryStructure.Result {
    const { registry, targetModule } = input;

    try {
      // Check registry structure
      if (!registry || typeof registry !== 'object') {
        throw new Error('Registry is null, undefined, or not an object');
      }

      if (!registry.modules || typeof registry.modules !== 'object') {
        throw new Error('Registry.modules is missing or invalid');
      }

      if (!registry.dependencyGraph || typeof registry.dependencyGraph !== 'object') {
        throw new Error('Registry.dependencyGraph is missing or invalid');
      }

      // Check target module exists if specified
      if (targetModule && !registry.modules[targetModule]) {
        throw new Error(`Target module '${targetModule}' not found in registry. Available modules: ${Object.keys(registry.modules).join(', ')}`);
      }

      // Check dependency graph structure
      if (!Array.isArray(registry.dependencyGraph.circularDependencies)) {
        throw new Error('Registry.dependencyGraph.circularDependencies is missing or not an array');
      }

      console.log('✅ Registry sanity checks passed');
      return {};

    } catch (error) {
      throw new Error(`💥 REGISTRY CORRUPTION: ${error.message}`);
    }
  }
}