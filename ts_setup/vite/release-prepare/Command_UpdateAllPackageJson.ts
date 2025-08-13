import { existsSync, readFileSync, writeFileSync } from "node:fs";
import { join } from "node:path";
import { BuildConfig, ModuleRegistry, VersionsFile } from "../module-registry";

import { Command_RemoveInternalDependencies } from './Command_RemoveInternalDependencies'
import { Command_UpdatePeerDependencies } from './Command_UpdatePeerDependencies'


export declare namespace Command_UpdateAllPackageJson {
  export interface Input {
    rootPath: string;
    registry: ModuleRegistry;
    updatedVersions: VersionsFile;
    changedProfiles: string[];
  }
  
  export interface Result {
    updatedPackages: string[];
    trace: Record<string, BuildConfig>
  }
}

export class Command_UpdateAllPackageJson {
  execute(input: Command_UpdateAllPackageJson.Input): Command_UpdateAllPackageJson.Result {
    const { registry, updatedVersions, changedProfiles } = input;
    
    console.log(`📝 Updating package.json versions for changed profiles`);
    
    const updatedPackages: string[] = [];
    const trace: Record<string, BuildConfig> = {};
    
    for (const profileName of changedProfiles) {
      const versionEntry = updatedVersions[profileName];
      const moduleInfo = registry.modules[versionEntry.moduleName];
      
      if (!moduleInfo) {
        throw new Error(`❌  Module not found: ${versionEntry.moduleName}`);
      }
      const packageJsonPath = join(input.rootPath, moduleInfo.path, 'package.json');
      
      if (!existsSync(packageJsonPath)) {
        throw new Error(`❌  package.json not found: ${packageJsonPath}`);
      }

      const packageJson = JSON.parse(readFileSync(packageJsonPath, 'utf-8'));
      new Command_RemoveInternalDependencies().execute({ moduleInfo, packageJson, registry });
      const { trace: moduleTrace } = new Command_UpdatePeerDependencies().execute({ rootPath: input.rootPath, moduleInfo, packageJson, registry });

      trace[versionEntry.moduleName] = moduleTrace;
      
      packageJson.version = versionEntry.version;
      packageJson.main = "dist/index.js";
      packageJson.types = "dist/index.d.ts";   
      packageJson.files = ["dist"];
      packageJson.private = 'false';

      writeFileSync(packageJsonPath, JSON.stringify(packageJson, null, 2), 'utf-8');
      updatedPackages.push(versionEntry.moduleName);
      
      console.log(`   ✅ ${versionEntry.moduleName}: updated to v${versionEntry.version}`);
    }
    
    return { updatedPackages, trace };
  }
}