import { VersionsFile } from "../module-registry";

export declare namespace Command_DetectChangesAndBump {
  export interface Input {
    versionsFile: VersionsFile;
    profileHashes: Record<string, string>;
  }
  
  export interface Result {
    updatedVersions: VersionsFile;
    changedProfiles: string[];
    unchangedProfiles: string[];
  }
}

export class Command_DetectChangesAndBump {
  execute(input: Command_DetectChangesAndBump.Input): Command_DetectChangesAndBump.Result {
    const { versionsFile, profileHashes } = input;
    
    console.log(`🔄 Detecting changes and bumping versions`);
    
    const updatedVersions: VersionsFile = { ...versionsFile };
    const changedProfiles: string[] = [];
    const unchangedProfiles: string[] = [];
    
    for (const [profileName, currentHash] of Object.entries(profileHashes)) {
      const versionEntry = updatedVersions[profileName];
      
      if (!versionEntry) {
        console.warn(`⚠️  No version entry found for profile: ${profileName}`);
        continue;
      }
      
      if (versionEntry.srcHash === currentHash) {
        // No changes
        unchangedProfiles.push(profileName);
        console.log(`   ✅ ${profileName}: no changes (${currentHash.substring(0, 8)}...)`);
      } else {
        // Hash changed - bump version
        const newVersion = _bumpVersion(versionEntry.version);
        
        updatedVersions[profileName] = {
          ...versionEntry,
          version: newVersion,
          srcHash: currentHash,
          lastPublished: new Date().toISOString()
        };
        
        changedProfiles.push(profileName);
        console.log(`   🔄 ${profileName}: v${versionEntry.version} → v${newVersion} (hash changed)`);
      }
    }
    
    console.log(`📊 Summary: ${changedProfiles.length} changed, ${unchangedProfiles.length} unchanged`);
    
    return {
      updatedVersions,
      changedProfiles,
      unchangedProfiles
    };
  }
}


function _bumpVersion(version: string): string {
  const parts = version.split('.').map(Number);
  if (parts.length !== 3) return '1.0.1';
  
  parts[2]++; // Bump patch version
  return parts.join('.');
}