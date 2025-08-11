import { join } from "node:path";
import { ModuleRegistry, VersionsFile } from "../module-registry"
import { Command_RunPnpmCommand } from "./Command_RunPnpmCommand"


export declare namespace Command_PublishChangedModules {
  export interface Input {
    registry: ModuleRegistry;
    changedProfiles: string[];
    updatedVersions: VersionsFile;
  }
  
  export interface Result {
    publishedModules: string[];
    failedPublishes: { moduleName: string; error: string }[];
  }
}

export class Command_PublishChangedModules {
  execute(input: Command_PublishChangedModules.Input): Command_PublishChangedModules.Result {
    const { registry, changedProfiles, updatedVersions } = input;
    
    console.log(`📦 Publishing ${changedProfiles.length} changed modules`);
    
    const publishedModules: string[] = [];
    const failedPublishes: { moduleName: string; error: string }[] = [];
    const pnpmCmd = new Command_RunPnpmCommand();
    
    for (const profileName of changedProfiles) {
      const versionEntry = updatedVersions[profileName];
      const moduleInfo = registry.modules[versionEntry.moduleName];
      const modulePath = join(registry.rootPath, moduleInfo.path);
      
      console.log(`   📤 Publishing ${versionEntry.moduleName} v${versionEntry.version}...`);
      
      const result = pnpmCmd.execute({
        command: 'publish',
        args: ['--access', 'public'],
        cwd: modulePath,
        timeout: 60000 // 1 minute
      });
      
      if (result.success) {
        publishedModules.push(versionEntry.moduleName);
        console.log(`   ✅ ${versionEntry.moduleName}: published successfully`);
      } else {
        failedPublishes.push({
          moduleName: versionEntry.moduleName,
          error: result.stderr || result.stdout
        });
        console.error(`   ❌ ${versionEntry.moduleName}: publish failed`);
      }
    }
    
    if (failedPublishes.length > 0) {
      throw new Error(`${failedPublishes.length} publishes failed: ${failedPublishes.map(f => f.moduleName).join(', ')}`);
    }
    
    console.log(`✅ All ${publishedModules.length} modules published successfully`);
    
    return { publishedModules, failedPublishes };
  }
}