import { execSync } from "node:child_process";
import { BuildProfile, VersionsFile } from "../module-registry";

export declare namespace Command_ResolveInitialVersions {
  export interface Input {
    buildProfiles: BuildProfile[];
    existingVersions: VersionsFile;
  }
  
  export interface Result {
    initialVersions: VersionsFile;
  }
}

export class Command_ResolveInitialVersions {
  execute(input: Command_ResolveInitialVersions.Input): Command_ResolveInitialVersions.Result {
    const { buildProfiles, existingVersions } = input;
    
    console.log(`🔍 Resolving initial versions for ${buildProfiles.length} build profiles`);
    
    const initialVersions: VersionsFile = { ...existingVersions };
    
    for (const profile of buildProfiles) {
      if (!initialVersions[profile.name]) {
        console.log(`   🆕 New profile: ${profile.name}`);
        
        // Try to get version from npm registry
        const npmVersion = _getNpmVersion(profile.entryModule);
        const initialVersion = npmVersion || '1.0.0';
        
        initialVersions[profile.name] = {
          moduleName: profile.entryModule,
          version: initialVersion,
          srcHash: '', // Will be populated by hash command
          lastPublished: npmVersion ? new Date().toISOString() : ''
        };
        
        console.log(`   📦 ${profile.name}: initialized at v${initialVersion}`);
      } else {
        console.log(`   ✅ ${profile.name}: existing v${initialVersions[profile.name].version}`);
      }
    }
    
    return { initialVersions };
  }
}


function _getNpmVersion(packageName: string): string | null {
  try {
    const output = execSync(`npm view ${packageName} version`, { 
      encoding: 'utf-8',
      stdio: 'pipe'
    });
    return output.trim();
  } catch (error) {
    return null;
  }
}
