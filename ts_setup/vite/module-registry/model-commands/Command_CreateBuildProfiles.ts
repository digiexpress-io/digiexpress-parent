import { ModuleInfo, BuildProfile } from '../module-registry-types';


export declare namespace Command_CreateBuildProfiles {
  export interface Input {
    moduleInfos: ModuleInfo[];
  }

  export interface Result {
    buildProfiles: BuildProfile[];
  }
}

export class Command_CreateBuildProfiles {
  execute(input: Command_CreateBuildProfiles.Input): Command_CreateBuildProfiles.Result {
    const { moduleInfos } = input;

    console.log(`⚙️  Generating build profiles...`);

    const profiles: BuildProfile[] = [];

    // Generate a build profile for each library module (modules that start with 'lib-')
    for (const moduleInfo of moduleInfos) {
      const folderName = moduleInfo.path.split('/').pop() || '';

      if (folderName.startsWith('lib-')) {
        const profile: BuildProfile = {
          name: folderName,
          entryPoint: `${moduleInfo.path}/index.ts`,
          path: moduleInfo.path,
          entryModule: moduleInfo.name,
          includedModules: [moduleInfo.name, ...moduleInfo.internalDependencies],
          externalDependencies: _collectExternalDependenciesFromTree(moduleInfo, moduleInfos)
        };

        profiles.push(profile);
        console.log(`   📋 Profile: ${profile.name} (${profile.includedModules.length} modules)`);
      }
    }

    console.log(`✅ Generated ${profiles.length} build profiles`);
    return { buildProfiles: profiles };
  }
}

// Pure transformative functions
function _collectExternalDependenciesFromTree(rootModule: ModuleInfo, allModules: ModuleInfo[]): string[] {
  const visited = new Set<string>();
  const externalDeps = new Set<string>();
  const moduleMap = new Map<string, ModuleInfo>();

  // Create lookup map for efficient module resolution
  for (const module of allModules) {
    moduleMap.set(module.name, module);
  }

  const traverse = (moduleName: string) => {
    if (visited.has(moduleName)) return;

    const moduleInfo = moduleMap.get(moduleName);
    if (!moduleInfo) {
      console.warn(`⚠️  Module '${moduleName}' not found during external dependency collection`);
      return;
    }

    visited.add(moduleName);

    // Collect external dependencies from this module
    moduleInfo.externalDependencies.forEach(dep => externalDeps.add(dep));

    // Recursively traverse internal dependencies
    moduleInfo.internalDependencies.forEach(internalDep => {
      traverse(internalDep);
    });
  };

  // Start traversal from root module
  traverse(rootModule.name);

  return Array.from(externalDeps).sort(); // Sort for consistency
}