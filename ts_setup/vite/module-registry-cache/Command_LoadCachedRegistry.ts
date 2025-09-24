import { existsSync, readFileSync } from 'node:fs';
import { ModuleRegistry } from '../module-registry'

export declare namespace Command_LoadCachedRegistry {
  export interface Input {
    fullRegistryPath: string;
  }
  
  export interface Result {
    cachedRegistry?: ModuleRegistry;
    exists: boolean;
  }
}

export class Command_LoadCachedRegistry {
  execute(input: Command_LoadCachedRegistry.Input): Command_LoadCachedRegistry.Result {
    const { fullRegistryPath } = input;
    
    if (!existsSync(fullRegistryPath)) {
      return { exists: false };
    }

    console.log(`📋 Found cached registry: ${fullRegistryPath}`);
    
    try {
      const cachedRegistry: ModuleRegistry = JSON.parse(
        readFileSync(fullRegistryPath, 'utf-8')
      );
      
      return { cachedRegistry, exists: true };
    } catch (error) {
      console.warn(`⚠️  Failed to load cached registry: ${error}`);
      return { exists: false };
    }
  }
}