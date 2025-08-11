import { existsSync, readFileSync } from "node:fs";
import { ModuleRegistry } from "../module-registry";

export declare namespace Command_ValidateRegistry {
  export interface Input {
    registryPath?: string;
  }
  
  export interface Result {
    registry: ModuleRegistry;
  }
}

export class Command_ValidateRegistry {
  execute(input: Command_ValidateRegistry.Input): Command_ValidateRegistry.Result {
    const { registryPath = '.modules/registry.json' } = input;
    
    console.log(`🔍 Validating registry: ${registryPath}`);
    
    if (!existsSync(registryPath)) {
      throw new Error(`Registry file not found: ${registryPath}`);
    }
    
    try {
      const content = readFileSync(registryPath, 'utf-8');
      const registry = JSON.parse(content);
      
      if (!registry.modules || !registry.buildProfiles) {
        throw new Error('Invalid registry structure - missing modules or buildProfiles');
      }
      
      console.log(`✅ Registry valid: ${Object.keys(registry.buildProfiles).length} build profiles`);
      
      return { registry };
      
    } catch (error) {
      throw new Error(`Failed to load registry: ${error.message}`);
    }
  }
}
