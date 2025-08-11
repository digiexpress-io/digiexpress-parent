import { ModuleRegistry, VersionsFile } from "../module-registry";
import { ReleaseRollbackBuilder } from "../release-prepare";


export declare namespace Command_RestorePackageJsons {
  export interface Input {
    registry: ModuleRegistry;
    changedProfiles: string[];
    updatedVersions: VersionsFile;
  }
  
  export interface Result {
    restoredModules: string[];
  }
}

export class Command_RestorePackageJsons {
  execute(input: Command_RestorePackageJsons.Input): Command_RestorePackageJsons.Result {
    const { registry, changedProfiles, updatedVersions } = input;
    
    console.log(`♻️  Restoring package.json files for ${changedProfiles.length} modules`);
    
    const restoredModules: string[] = [];
    const restoreBuilder = new ReleaseRollbackBuilder();
    
    for (const profileName of changedProfiles) {
      const versionEntry = updatedVersions[profileName];
      
      try {
        restoreBuilder.build(registry, versionEntry.moduleName);
        restoredModules.push(versionEntry.moduleName);
        console.log(`   ✅ ${versionEntry.moduleName}: restored`);
      } catch (error) {
        console.warn(`   ⚠️  ${versionEntry.moduleName}: restore failed - ${error.message}`);
      }
    }
    
    console.log(`✅ Restored ${restoredModules.length} package.json files`);
    
    return { restoredModules };
  }
}