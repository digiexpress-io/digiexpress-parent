import { dirname, extname, join } from "node:path";
import { existsSync, mkdirSync, readdirSync, statSync, writeFileSync } from "node:fs";

import { BuildConfig, ModuleRegistry, ReleaseCommitLog, VersionsFile } from "../module-registry";


interface GeneratedFile {
  name: string;
  size: number;
}

interface LibrarySummary {
  libraryName: string;
  version: string;
  generatedFiles: GeneratedFile[];
  moduleLogs: ReleaseCommitLog[];
  internalDependencies: string[];
  externalDependencies: Record<string, string>;
}


export declare namespace Command_SaveLogFile {
  
  export interface Input {
    rootPath: string;
    registry: ModuleRegistry;
    versionsFile: VersionsFile;
    changedProfiles: string[];
    moduleLogs: ReleaseCommitLog[];
    trace: Record<string, BuildConfig>;
  }
  
  export interface Result {

  }
}

export class Command_SaveLogFile {
  execute(input: Command_SaveLogFile.Input): Command_SaveLogFile.Result {
    for(const profileName of input.changedProfiles) {
      const profile = input.registry.buildProfiles[profileName];
      const versionEntry = input.versionsFile[profileName];
      const moduleInfo = input.registry.modules[versionEntry.moduleName];

      const moduleTrace = input.trace[versionEntry.moduleName];
      

      const summary: LibrarySummary = {
        libraryName: moduleInfo.name,
        version: versionEntry.version,
        generatedFiles: _findGeneratedFiles(join(input.rootPath, profile.path, 'dist')),
        internalDependencies: moduleTrace.metadata.internalDependencies,
        externalDependencies: moduleTrace.metadata.externalDependencies,
        moduleLogs: input.moduleLogs,
      }
      
      const distLogPath = join(input.rootPath, moduleInfo.path, 'dist', 'gitlog.json');
      const dir = dirname(distLogPath);


      if (!existsSync(dir)) {
        mkdirSync(dir, { recursive: true });
      }
      writeFileSync(distLogPath, JSON.stringify(summary, null, 2), 'utf-8');
    }

    return {  };
  }
}


const targetExtensions = ['.js', '.d.ts', '.css', '.mjs', '.cjs', '.map'];

function _findGeneratedFiles(path: string): GeneratedFile[] {  
  const generatedFiles: GeneratedFile[] = [];
  _scanDirectory(path, generatedFiles);
  return generatedFiles.sort((a, b) => a.name.localeCompare(b.name));
}

function _scanDirectory(currentPath: string, generatedFiles: GeneratedFile[]): void {
  try {
    const entries = readdirSync(currentPath);

    for (const entry of entries) {
      const fullPath = join(currentPath, entry);
      const stats = statSync(fullPath);

      if (stats.isDirectory()) {
        // Recursive scan
        _scanDirectory(fullPath, generatedFiles);
      } else if (stats.isFile()) {
        const ext = extname(entry);
        
        // Special case for .d.ts files (check before .ts)
        if (entry.endsWith('.d.ts') || targetExtensions.includes(ext)) {
          generatedFiles.push({
            name: entry,
            size: stats.size
          });
        }
      }
    }
  } catch (error) {
    console.warn(`⚠️ Failed to scan directory ${currentPath}: ${error}`);
  }
}