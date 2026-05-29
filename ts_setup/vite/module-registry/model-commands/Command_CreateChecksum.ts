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
    const { moduleInfos: rawModuleInfos, rootPath } = input;

    const moduleInfos: ModuleInfo[] = JSON.parse(JSON.stringify(rawModuleInfos));

    const hash = createHash('sha256');

    // Sort modules by name for consistent checksum
    const sortedModules = [...moduleInfos].sort((a, b) => a.name.localeCompare(b.name));

    for (const moduleInfo of sortedModules) {
      // Add module name and package.json content
      hash.update(moduleInfo.name);
      hash.update(moduleInfo.path);
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
    }

    const checksum = hash.digest('hex');
    console.log(`✅ Checksum calculated: ${checksum.substring(0, 12)}...`);

    return { checksum };
  }
}
