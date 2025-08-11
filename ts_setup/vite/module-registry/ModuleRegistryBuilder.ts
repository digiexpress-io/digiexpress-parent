import { resolve } from 'node:path';

import { BuildProfile, ModuleInfo, ModuleRegistry } from './module-registry-types';
import { Command_CreateBuildProfiles } from './Command_CreateBuildProfiles';
import { Command_FindImports } from './Command_FindImports';
import { Command_FindModules } from './Command_FindModules';
import { Command_CreateDepGraph } from './Command_CreateDepGraph';
import { Command_CreateChecksum } from './Command_CreateChecksum';


export class ModuleRegistryBuilder {
 private rootPath: string;

 constructor(rootPath: string) {
   this.rootPath = resolve(rootPath);
 }

 build(): ModuleRegistry {
   console.log(`🔍 Building module registry from: ${this.rootPath}`);

   // Command pipeline orchestration
   const findModulesCmd = new Command_FindModules();
   const findImportsCmd = new Command_FindImports();
   const createDepGraphCmd = new Command_CreateDepGraph();
   const createBuildProfilesCmd = new Command_CreateBuildProfiles();
   const createChecksumCmd = new Command_CreateChecksum();

   // Step 1: Discover modules
   const { moduleInfos: discoveredModules } = findModulesCmd.execute({
     rootPath: this.rootPath
   });

   // Step 2: Analyze source code usage
   const { moduleInfos: analyzedModules } = findImportsCmd.execute({
     moduleInfos: discoveredModules,
     rootPath: this.rootPath
   });

   // Step 3: Build dependency graph
   const { dependencyGraph } = createDepGraphCmd.execute({
     moduleInfos: analyzedModules
   });

   // Step 4: Generate build profiles
   const { buildProfiles } = createBuildProfilesCmd.execute({
     moduleInfos: analyzedModules
   });

   // Step 5: Calculate checksum
   const { checksum } = createChecksumCmd.execute({
     moduleInfos: analyzedModules,
     rootPath: this.rootPath
   });

   // Assemble final registry
   const registry: ModuleRegistry = {
     modules: _arrayToRecord(analyzedModules),
     dependencyGraph,
     buildProfiles: _buildProfilesToRecord(buildProfiles),
     rootPath: this.rootPath,
     checksum,
     generatedAt: new Date().toISOString()
   };

   console.log(`✅ Registry built with ${analyzedModules.length} modules (checksum: ${checksum.substring(0, 8)}...)`);
   return registry;
 }
}

// Pure transformative functions
function _arrayToRecord(moduleInfos: ModuleInfo[]): Record<string, ModuleInfo> {
 const record: Record<string, ModuleInfo> = {};
 for (const moduleInfo of moduleInfos) {
   record[moduleInfo.name] = moduleInfo;
 }
 return record;
}

function _buildProfilesToRecord(buildProfiles: BuildProfile[]): Record<string, BuildProfile> {
 const record: Record<string, BuildProfile> = {};
 for (const profile of buildProfiles) {
   record[profile.name] = profile;
 }
 return record;
}