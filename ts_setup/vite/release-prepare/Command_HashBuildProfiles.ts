import { createHash } from "node:crypto";
import { BuildProfile, ModuleRegistry } from "../module-registry";
import { extname, join } from "node:path";
import { existsSync, readdirSync, readFileSync, statSync } from "node:fs";

export declare namespace Command_HashBuildProfiles {
  export interface Input {
    rootPath: string;
    registry: ModuleRegistry;
    buildProfiles: BuildProfile[];
  }
  
  export interface Result {
    profileHashes: Record<string, string>;
  }
}

export class Command_HashBuildProfiles {
  execute(input: Command_HashBuildProfiles.Input): Command_HashBuildProfiles.Result {
    const { registry, buildProfiles } = input;
    
    console.log(`🔐 Hashing ${buildProfiles.length} build profiles`);
    
    const profileHashes: Record<string, string> = {};
    
    for (const profile of buildProfiles) {
      console.log(`   🔍 Hashing ${profile.name}...`);
      
      // Hash all included modules' source code
      const hash = createHash('sha256');
      
      for (const moduleName of profile.includedModules) {
        const moduleInfo = registry.modules[moduleName];
        if (moduleInfo) {
          const modulePath = join(input.rootPath, moduleInfo.path);
          _addModuleToHash(modulePath, hash);
        }
      }
      
      const profileHash = hash.digest('hex');
      profileHashes[profile.name] = profileHash;
      
      console.log(`   ✅ ${profile.name}: ${profileHash.substring(0, 12)}...`);
    }
    
    return { profileHashes };
  }
}



function _addModuleToHash(modulePath: string, hash: ReturnType<typeof createHash>): void {
  if (!existsSync(modulePath)) return;
  
  const entries = readdirSync(modulePath).sort();
  
  for (const entry of entries) {
    const fullPath = join(modulePath, entry);
    const stat = statSync(fullPath);
    
    if (stat.isDirectory()) {
      if (!['node_modules', 'dist', '.git'].includes(entry)) {
        hash.update(`dir:${entry}`);
        _addModuleToHash(fullPath, hash);
      }
    } else if (stat.isFile()) {
      const ext = extname(entry);
      if (['.ts', '.tsx', '.js', '.jsx'].includes(ext) || entry === 'package.json') {
        try {
          const content = readFileSync(fullPath, 'utf-8');
          hash.update(`file:${entry}`);
          hash.update(content);
        } catch (error) {
          hash.update(`file:${entry}:unreadable`);
        }
      }
    }
  }
}