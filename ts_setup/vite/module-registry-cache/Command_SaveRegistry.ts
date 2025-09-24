import { dirname } from 'node:path';
import { existsSync, mkdirSync, writeFileSync } from 'node:fs';

import { ModuleRegistry } from '../module-registry'



export declare namespace Command_SaveRegistry {
  export interface Input {
    registry: ModuleRegistry;
    fullRegistryPath: string;
    onRegistryBuilt?: (registry: ModuleRegistry) => void;
  }
  
  export interface Result {
    // void - saves file and calls callback
  }
}

export class Command_SaveRegistry {
  execute(input: Command_SaveRegistry.Input): Command_SaveRegistry.Result {
    const { registry, fullRegistryPath, onRegistryBuilt } = input;
    
    try {
      // Ensure directory exists
      const registryDir = dirname(fullRegistryPath);
      if (!existsSync(registryDir)) {
        mkdirSync(registryDir, { recursive: true });
      }
      
      // Save registry to file
      writeFileSync(fullRegistryPath, JSON.stringify(registry, null, 2), 'utf-8');
      console.log(`💾 Registry saved: ${fullRegistryPath}`);
      
      // Notify user that registry was rebuilt
      if (onRegistryBuilt) {
        console.log('🚀 Calling onRegistryBuilt hook...');
        onRegistryBuilt(registry);
        console.log('✅ onRegistryBuilt completed');
      }
      
      return {};
    } catch (error) {
      console.error('❌ Failed to save registry:', error);
      throw error;
    }
  }
}