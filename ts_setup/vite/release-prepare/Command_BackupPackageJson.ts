import { join } from "node:path";
import { existsSync, readFileSync, writeFileSync } from "node:fs";
import { BuildProfile, ModuleRegistry } from "../module-registry";


export declare namespace Command_BackupPackageJson {
  export interface Input {
    registry: ModuleRegistry;
    buildProfiles: BuildProfile[];
    changedProfiles: string[];
  }
  
  export interface Result {
    entries: ResultEntry[]
  }

  export interface ResultEntry {
    backupPath: string;
    originalPath: string;
  }
}

export class Command_BackupPackageJson {
  execute(input: Command_BackupPackageJson.Input): Command_BackupPackageJson.Result {
    const { registry, buildProfiles } = input;
    const entries: Command_BackupPackageJson.ResultEntry[] = [];
    for(const { entryModule: moduleName, name } of buildProfiles) {
  
      if(!input.changedProfiles.includes(name)) {
        console.log(`📋 Skipping package.json for ${moduleName}`);
        continue;
      }
      
      const moduleInfo = registry.modules[moduleName];
      if (!moduleInfo) {
        throw new Error(`Module '${moduleName}' not found in registry`);
      }
      
      const modulePath = join(registry.rootPath, moduleInfo.path);
      const originalPath = join(modulePath, 'package.json');
      const backupPath = join(modulePath, 'package.json.versionsbackup');
      
      if (!existsSync(originalPath)) {
        throw new Error(`package.json not found at: ${originalPath}`);
      }
      
      console.log(`📋 Backing up package.json for ${moduleName}`);
      const content = readFileSync(originalPath, 'utf-8');
      writeFileSync(backupPath, content, 'utf-8');
      
      console.log(`✅ Backup created: ${backupPath}`);
      entries.push({ backupPath, originalPath });
    }
    return { entries }
  }
}