import { readFileSync, existsSync, readdirSync, statSync } from 'node:fs';
import { resolve, join } from 'node:path';
import { ModuleInfo } from './module-registry-types';


export declare namespace Command_FindModules {
  export interface Input {
    rootPath: string;
  }

  export interface Result {
    moduleInfos: ModuleInfo[];
  }
}

export class Command_FindModules {
  execute(input: Command_FindModules.Input): Command_FindModules.Result {
    const { rootPath } = input;
    const resolvedRootPath = resolve(rootPath);
    const modulesPath = join(resolvedRootPath, 'modules');

    console.log(`📂 Discovering modules in: ${modulesPath}`);

    if (!existsSync(modulesPath)) {
      throw new Error(`Modules directory not found: ${modulesPath}`);
    }

    const moduleInfos: ModuleInfo[] = [];
    const entries = readdirSync(modulesPath);

    for (const entry of entries) {
      const modulePath = join(modulesPath, entry);
      const stat = statSync(modulePath);

      if (stat.isDirectory()) {
        const packageJsonPath = join(modulePath, 'package.json');

        if (existsSync(packageJsonPath)) {
          try {
            const moduleInfo = this.parseModuleInfo(modulePath, resolvedRootPath);
            moduleInfos.push(moduleInfo);
            console.log(`   📦 Found module: ${moduleInfo.name} (${entry})`);
          } catch (error) {
            console.warn(`   ⚠️  Failed to parse module: ${entry} - ${error}`);
          }
        } else {
          console.log(`   ⏭️  Skipped directory (no package.json): ${entry}`);
        }
      }
    }

    console.log(`✅ Discovered ${moduleInfos.length} modules`);
    return { moduleInfos };
  }

  private parseModuleInfo(modulePath: string, rootPath: string): ModuleInfo {
    const packageJsonPath = join(modulePath, 'package.json');
    const packageJson = JSON.parse(readFileSync(packageJsonPath, 'utf-8'));

    // Extract dependencies (ignore devDependencies)
    const dependencies = Object.keys(packageJson.dependencies || {});
    const peerDependencies = Object.keys(packageJson.peerDependencies || {});

    // Combine dependencies and peerDependencies for analysis
    const allDependencies = [...new Set([...dependencies, ...peerDependencies])];

    // Split into internal (@dxs-ts) and external dependencies
    const internalDependencies = allDependencies.filter(dep => dep.startsWith('@dxs-ts/'));
    const externalDependencies = allDependencies.filter(dep => !dep.startsWith('@dxs-ts/'));

    return {
      name: packageJson.name,
      path: modulePath.replace(rootPath + '/', ''), // Relative path
      packageJson,
      dependencies: allDependencies,
      peerDependencies,
      externalDependencies,
      internalDependencies,

      // Initialize actual usage data (will be populated by Command_FindImports)
      actualDependencies: [],
      actualExternalDependencies: [],
      actualInternalDependencies: [],
      missingDependencies: [],
      unusedDependencies: []
    };
  }
}