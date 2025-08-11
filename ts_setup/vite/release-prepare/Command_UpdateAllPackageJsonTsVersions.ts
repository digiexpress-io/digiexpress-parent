import { existsSync, writeFileSync } from "node:fs";
import { join } from "node:path";
import { BuildConfig, ModuleRegistry, VersionsFile, ModuleCommits } from "../module-registry";



export declare namespace Command_UpdateAllPackageJsonTsVersions {
  export interface Input {
    registry: ModuleRegistry;
    updatedVersions: VersionsFile;
    changedProfiles: string[];
    trace: Record<string, BuildConfig>;
    moduleCommits: ModuleCommits;
  }
  
  export interface Result {
    updatedPackages: string[];
  }
}

export class Command_UpdateAllPackageJsonTsVersions {
  execute(input: Command_UpdateAllPackageJsonTsVersions.Input): Command_UpdateAllPackageJsonTsVersions.Result {
    const { registry, updatedVersions, changedProfiles, trace } = input;
    const now = new Date();
    console.log(`📝 Updating version.ts versions for changed profiles`);
    
    const updatedPackages: string[] = [];
    
    for (const profileName of changedProfiles) {
      const versionEntry = updatedVersions[profileName];
      const moduleInfo = registry.modules[versionEntry.moduleName];
      
      if (!moduleInfo) {
        throw new Error(`❌  Module not found: ${versionEntry.moduleName}`);
      }
      const versionTsPath = join(registry.rootPath, moduleInfo.path, 'version.ts');
      
      if (!existsSync(versionTsPath)) {
        throw new Error(`❌  version.ts not found: ${versionTsPath}`);
      }
      
      const date_formated = _formatDate(now);
      const moduleTrace = trace[versionEntry.moduleName];
      const versionTs = [
        `import { VersionInfoApi } from "@dxs-ts/envir-util";`,
        `export const version = '${versionEntry.version}';export const build_time = '${date_formated}';`,
        `export const renderVersion = () => VersionInfoApi.builder()
          .setLogo('logo_1_great_ones_wisdom')
          .setTheme('red')
          .setCommits(${JSON.stringify(input.moduleCommits)})
          .setProjectInfo('${moduleInfo.name}', '${versionEntry.version}', '${date_formated}')
          .addInternalComponents([${moduleTrace.metadata.internalDependencies.map(e => "'"+ e + "'").join(',')}])
          .addExternalComponents([${Object.entries(moduleTrace.metadata.externalDependencies).map(e => "'"+ e[0] + "@" + e[1] + "'").join(',')}])
          .render();`
      ].join('\n');

      writeFileSync(versionTsPath, versionTs, 'utf-8');
      updatedPackages.push(versionEntry.moduleName);
      
      console.log(`   ✅ ${versionEntry.moduleName}: updated to v${versionEntry.version}`);
    }
    
    return { updatedPackages };
  }
}

function _formatDate(now: Date) {
  const formatted = now.toLocaleString('en-US', {
    month: '2-digit',
    day: '2-digit', 
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  });
  return formatted; // "10/07/2025 07:06:54"
}