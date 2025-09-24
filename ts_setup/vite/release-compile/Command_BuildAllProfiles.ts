import { join } from "node:path"
import { BuildProfile, ModuleRegistry } from "../module-registry"
import { Command_RunNodeCommand } from "./Command_RunNodeCommand"


export declare namespace Command_BuildAllProfiles {
  export interface Input {
    buildProfiles: BuildProfile[];
    registry: ModuleRegistry;
  }
  
  export interface Result {
    successfulBuilds: string[];
    failedBuilds: { profileName: string; error: string }[];
  }
}

export class Command_BuildAllProfiles {
  execute(input: Command_BuildAllProfiles.Input): Command_BuildAllProfiles.Result {
    const { buildProfiles, registry } = input;
    
    console.log(`🏗️  Building ${buildProfiles.length} profiles`);
    
    const successfulBuilds: string[] = [];
    const failedBuilds: { profileName: string; error: string }[] = [];
    const pnpmCmd = new Command_RunNodeCommand();
    
    for (const profile of buildProfiles) {
      const moduleInfo = registry.modules[profile.entryModule];
      
      console.log(`   🔨 Building ${profile.name}...`);
      
      const result = pnpmCmd.execute({
        command: '',
        args: [
          'START_MODE=uni-build',
          `UNI_BUILD_MODULE='${moduleInfo.packageJson.name}'`,
          'pnpm exec',
          'vite',
          'build'
        ],
        timeout: 120000 // 2 minutes
      });
      
      if (result.success) {
        successfulBuilds.push(profile.name);
        console.log(`   ✅ ${profile.name}: build successful`);
      } else {
        failedBuilds.push({ 
          profileName: profile.name, 
          error: result.stderr || result.stdout 
        });
        console.error(`   ❌ ${profile.name}: build failed`);
      }
    }
    
    if (failedBuilds.length > 0) {
      throw new Error(`${failedBuilds.length} builds failed: ${failedBuilds.map(f => f.profileName).join(', ')}`);
    }
    
    console.log(`✅ All ${successfulBuilds.length} builds completed successfully`);
    
    return { successfulBuilds, failedBuilds };
  }
}