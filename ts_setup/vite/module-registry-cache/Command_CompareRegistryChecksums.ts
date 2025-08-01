import { ModuleRegistry, ModuleRegistryBuilder } from '../module-registry'

export declare namespace Command_CompareRegistryChecksums {
  export interface Input {
    rootPath: string;
    cachedRegistry?: ModuleRegistry;
  }
  
  export interface Result {
    currentRegistry: ModuleRegistry;
    needsRebuild: boolean;
  }
}

export class Command_CompareRegistryChecksums {
  execute(input: Command_CompareRegistryChecksums.Input): Command_CompareRegistryChecksums.Result {
    const { rootPath, cachedRegistry } = input;
    
    console.log('🔐 Calculating current checksum...');
    const builder = new ModuleRegistryBuilder(rootPath);
    const currentRegistry = builder.build();
    
    if (!cachedRegistry) {
      return { currentRegistry, needsRebuild: true };
    }
    
    // Compare checksums
    if (cachedRegistry.checksum === currentRegistry.checksum) {
      console.log(`✅ Registry up to date (checksum: ${cachedRegistry.checksum.substring(0, 8)}...)`);
      return { currentRegistry, needsRebuild: false };
    } else {
      console.log(`🔄 Registry outdated (${cachedRegistry.checksum.substring(0, 8)}... → ${currentRegistry.checksum.substring(0, 8)}...)`);
      return { currentRegistry, needsRebuild: true };
    }
  }
}
