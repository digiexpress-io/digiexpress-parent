import { resolve } from 'node:path';
import { ModuleRegistry, ModuleRegistryBuilder } from '../module-registry'
import { Command_LoadCachedRegistry } from './Command_LoadCachedRegistry';
import { Command_CompareRegistryChecksums } from './Command_CompareRegistryChecksums';
import { Command_SaveRegistry } from './Command_SaveRegistry';



export interface ModuleRegistryCacheOptions {
  registryPath?: string;
  onRegistryBuilt?: (registry: ModuleRegistry) => void;
}

export class ModuleRegistryCacheBuilder {
  private options: Required<ModuleRegistryCacheOptions>;

  constructor(options: ModuleRegistryCacheOptions = {}) {
    this.options = {
      registryPath: '.modules/registry.json',
      onRegistryBuilt: () => {},
      ...options
    };
  }

  build(rootPath: string, forceRebuild: boolean = false): ModuleRegistry {
    const fullRegistryPath = resolve(rootPath, this.options.registryPath);
    
    console.log(`🔍 Module Registry Cache Builder: ${rootPath}`);
    
    try {
      if (forceRebuild) {
        console.log('🔄 Force rebuild requested');
        return this.rebuildRegistry(rootPath, fullRegistryPath);
      } else {
        return this.checkAndBuildRegistry(rootPath, fullRegistryPath);
      }
    } catch (error) {
      console.error('❌ Failed to build module registry:', error);
      throw error;
    }
  }

  private checkAndBuildRegistry(rootPath: string, fullRegistryPath: string): ModuleRegistry {
    // Command pipeline orchestration
    const loadCachedCmd = new Command_LoadCachedRegistry();
    const compareChecksumsCmd = new Command_CompareRegistryChecksums();
    const saveRegistryCmd = new Command_SaveRegistry();

    // Step 1: Try to load cached registry
    const { cachedRegistry, exists } = loadCachedCmd.execute({
      fullRegistryPath
    });

    if (!exists) {
      console.log(`📂 No cached registry found, building new one...`);
      return this.rebuildRegistry(rootPath, fullRegistryPath);
    }

    // Step 2: Compare checksums
    const { currentRegistry, needsRebuild } = compareChecksumsCmd.execute({
      rootPath,
      cachedRegistry
    });

    // Step 3: Save if rebuild needed
    if (needsRebuild) {
      saveRegistryCmd.execute({
        registry: currentRegistry,
        fullRegistryPath,
        onRegistryBuilt: this.options.onRegistryBuilt
      });
    }

    return currentRegistry;
  }

  private rebuildRegistry(rootPath: string, fullRegistryPath: string): ModuleRegistry {
    const saveRegistryCmd = new Command_SaveRegistry();

    console.log('🏗️  Building module registry...');
    const builder = new ModuleRegistryBuilder(rootPath);
    const registry = builder.build();
    
    saveRegistryCmd.execute({
      registry,
      fullRegistryPath,
      onRegistryBuilt: this.options.onRegistryBuilt
    });

    return registry;
  }
}