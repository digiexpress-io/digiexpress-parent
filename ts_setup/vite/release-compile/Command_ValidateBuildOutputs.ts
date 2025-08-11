import { join } from "node:path";
import { BuildProfile, ModuleRegistry } from "../module-registry";
import { BuildConfig } from '../module-registry';
import { existsSync, readFileSync } from "node:fs";

export declare namespace Command_ValidateBuildOutputs {
  export interface Input {
    registry: ModuleRegistry;
    buildProfiles: BuildProfile[];
  }
  
  export interface Result {
    validProfiles: BuildProfile[];
    invalidProfiles: { profile: BuildProfile; reason: string }[];
  }
}

export class Command_ValidateBuildOutputs {
  execute(input: Command_ValidateBuildOutputs.Input): Command_ValidateBuildOutputs.Result {
    const { registry, buildProfiles } = input;
    
    console.log(`🔍 Validating build outputs for ${buildProfiles.length} profiles`);
    
    const validProfiles: BuildProfile[] = [];
    const invalidProfiles: { profile: BuildProfile; reason: string }[] = [];
    
    for (const profile of buildProfiles) {
      const moduleInfo = registry.modules[profile.entryModule];
      if (!moduleInfo) {
        invalidProfiles.push({ profile, reason: `Module not found: ${profile.entryModule}` });
        continue;
      }
      
      const distPath = join(registry.rootPath, moduleInfo.path, 'dist');
      const tracePath = join(distPath, 'trace.json');
      
      // Check if dist folder exists
      if (!existsSync(distPath)) {
        invalidProfiles.push({ profile, reason: `Dist folder not found: ${distPath}` });
        continue;
      }
      
      // Check if trace.json exists
      if (!existsSync(tracePath)) {
        invalidProfiles.push({ profile, reason: `trace.json not found: ${tracePath}` });
        continue;
      }
      
      // Validate trace.json structure
      try {
        const traceContent = readFileSync(tracePath, 'utf-8');
        const trace: BuildConfig = JSON.parse(traceContent);
        
        if (!trace.targetModuleInfo || !trace.buildProfile || !trace.build) {
          invalidProfiles.push({ profile, reason: `Invalid trace.json structure` });
          continue;
        }
        
        validProfiles.push(profile);
        console.log(`   ✅ ${profile.name}: valid`);
        
      } catch (error) {
        invalidProfiles.push({ profile, reason: `Failed to parse trace.json: ${error}` });
      }
    }
    
    if (invalidProfiles.length > 0) {
      console.error(`❌ ${invalidProfiles.length} invalid profiles found:`);
      invalidProfiles.forEach(({ profile, reason }) => {
        console.error(`   - ${profile.name}: ${reason}`);
      });
    }
    
    console.log(`✅ ${validProfiles.length} valid profiles`);
    
    return { validProfiles, invalidProfiles };
  }
}
