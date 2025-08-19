import { readFileSync, readdirSync, statSync } from 'node:fs';
import { join, extname } from 'node:path';
import { createHash } from 'node:crypto';

import { ModuleInfo } from '../module-registry-types';


export declare namespace Command_CreateChecksum {
  export interface Input {
    moduleInfos: ModuleInfo[];
    rootPath: string;
  }

  export interface Result {
    checksum: string;
  }
}

export class Command_CreateChecksum {
  execute(input: Command_CreateChecksum.Input): Command_CreateChecksum.Result {
    const { moduleInfos, rootPath } = input;

    console.log(`🔐 Calculating ecosystem checksum...`);

    const hash = createHash('sha256');

    // Sort modules by name for consistent checksum
    const sortedModules = [...moduleInfos].sort((a, b) => a.name.localeCompare(b.name));

    for (const moduleInfo of sortedModules) {
      // Add module name and package.json content
      hash.update(moduleInfo.name);
      hash.update(JSON.stringify(moduleInfo.packageJson));

      // Add dependency analysis results
      hash.update(JSON.stringify({
        dependencies: moduleInfo.dependencies.sort((a, b) => b.localeCompare(a)),
        actualDependencies: moduleInfo.actualDependencies.sort((a, b) => b.localeCompare(a)),
        missingDependencies: moduleInfo.missingDependencies.sort((a, b) => b.localeCompare(a)),
        unusedDependencies: moduleInfo.unusedDependencies.sort((a, b) => b.localeCompare(a)),
        internalDependencies: moduleInfo.internalDependencies.sort((a, b) => b.localeCompare(a)),
        externalDependencies: moduleInfo.externalDependencies.sort((a, b) => b.localeCompare(a)),
        actualInternalDependencies: moduleInfo.actualInternalDependencies.sort((a, b) => b.localeCompare(a)),
        actualExternalDependencies: moduleInfo.actualExternalDependencies.sort((a, b) => b.localeCompare(a))
      }));

      // Add all source file contents
      const modulePath = join(rootPath, moduleInfo.path);
      _addDirectoryToHash(rootPath, modulePath, hash);
    }

    const checksum = hash.digest('hex');
    console.log(`✅ Checksum calculated: ${checksum.substring(0, 12)}...`);

    return { checksum };
  }
}

// Pure transformative functions
function _addDirectoryToHash(rootPath: string, dirPath: string, hash: ReturnType<typeof createHash>): void {
  const entries = readdirSync(dirPath).sort(); // Sort for consistent hashing

  for (const entry of entries) {
    const fullPath = join(dirPath, entry);
    const stat = statSync(fullPath);

    if (stat.isDirectory()) {
      // Skip node_modules and dist directories
      if (!['node_modules', 'dist', '.git'].includes(entry)) {
        hash.update(`dir:${entry}`);
        _addDirectoryToHash(rootPath, fullPath, hash);
      }
    } else if (stat.isFile()) {
      // Include TypeScript/JavaScript files and package.json
      const ext = extname(entry);
      const relative = fullPath.substring(rootPath.length);
      if (['.ts', '.tsx', '.js', '.jsx'].includes(ext) || entry === 'package.json') {
        try {
          const content = readFileSync(fullPath, 'utf-8');
          hash.update(`file:${relative}`);
          hash.update(content);
        } catch (error) {
          // If we can't read the file, just include its name
          hash.update(`file:${relative}:unreadable`);
        }
      }
    }
  }
}