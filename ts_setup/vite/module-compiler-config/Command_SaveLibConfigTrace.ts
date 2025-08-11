import { dirname } from 'node:path';
import { existsSync, mkdirSync, writeFileSync } from 'node:fs';

import { ModuleCompilerConfig } from './module-compiler-config-types'
import { BuildConfig } from '../module-registry';



export declare namespace Command_SaveLibConfigTrace {
  export interface Input {
    config: ModuleCompilerConfig;
    fullTracePath: string
  }
  
  export interface Result {
    // void - saves file and calls callback
  }
}

export class Command_SaveLibConfigTrace {
  execute(input: Command_SaveLibConfigTrace.Input): Command_SaveLibConfigTrace.Result {
    const { config, fullTracePath } = input;
    
    try {
      // Ensure directory exists
      const registryDir = dirname(fullTracePath);
      if (!existsSync(registryDir)) {
        mkdirSync(registryDir, { recursive: true });
      }

      const trace: BuildConfig = {
        build: {
          outDir: config.build.outDir,
          lib: config.build.lib
        },
        buildProfile: config.buildProfile!,
        metadata: config.metadata(),
        resolve: config.resolve,
        targetModuleInfo: config.targetModuleInfo
      };

      // Save registry to file
      writeFileSync(fullTracePath, JSON.stringify(trace, null, 2), 'utf-8');
      console.log(`💾 Lib config trace saved: ${fullTracePath}`);
      return {};
    } catch (error) {
      console.error('❌ Failed to save lib config trace:', error);
      throw error;
    }
  }
}