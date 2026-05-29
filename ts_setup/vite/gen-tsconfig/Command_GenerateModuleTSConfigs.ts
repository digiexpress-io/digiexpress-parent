import { ModuleRegistry } from '../module-registry'
import { TSConfigOutput } from './gen-tsconfig-types';


export declare namespace Command_GenerateModuleTSConfigs {
  export interface Input {
    registry: ModuleRegistry;
  }
  export interface Result {
    moduleConfigs: Array<{
      moduleName: string;
      outputPath: string;
      tsConfig: TSConfigOutput;
    }>;
  }
}

export class Command_GenerateModuleTSConfigs {
  execute(input: Command_GenerateModuleTSConfigs.Input): Command_GenerateModuleTSConfigs.Result {
    const { registry } = input;
    const moduleConfigs: Command_GenerateModuleTSConfigs.Result['moduleConfigs'] = [];

    // Generate tsconfig for each module
    for (const [moduleName, moduleInfo] of Object.entries(registry.modules)) {
      const paths: Record<string, string[]> = {};
      const references: Array<{ path: string }> = [];
      const alias: Record<string, string[]> = {};

      // Hardcoded paths (carry over from master config)
      paths['@emotion/styled'] = ['../../node_modules/@emotion/styled'];

      // Generate path mappings only for this module's internal dependencies
      for (const depModuleName of moduleInfo.internalDependencies) {
        const depModuleInfo = registry.modules[depModuleName];
        if (depModuleInfo) {
          // Transform path from root-relative to module-relative
          // From: "./modules/envir-fetch/index.ts" 
          // To: "../envir-fetch/index.ts"
          const relativePath = "./" + this._transformPathToModuleRelative(depModuleInfo.path);
          paths[depModuleName] = [relativePath];

          // Add project reference
          const referencePath = this._getModuleReferencePath(depModuleInfo.path);
          references.push({ path: referencePath });

          alias[moduleName] = [referencePath];

          console.log(` 📦 ${moduleName} → ${depModuleName} at ${relativePath}`);
        }
      }

      // Build module-specific tsconfig
      const tsConfig: TSConfigOutput = {
        compilerOptions: { paths },
        extends: '../../tsconfig.json',
        references,
        _generated: {
          by: 'ModuleTSConfigGenerator',
          at: new Date().toISOString(),
          forModule: moduleName,
          fromRegistry: registry.checksum.substring(0, 12),
          alias,
        }
      };

      // Determine output path
      const outputPath = `${moduleInfo.path}/tsconfig.gen.json`;

      moduleConfigs.push({
        moduleName,
        outputPath,
        tsConfig
      });

      /*
      moduleConfigs.push({

        moduleName,
        outputPath: `${moduleInfo.path}/tsconfig.json`,
        //@ts-ignore
        tsConfig: {
          "extends": "./tsconfig.gen.json"
        }
      })*/

      console.log(` ✅ Generated tsconfig for ${moduleName} at ${outputPath}`);
    }

    return { moduleConfigs };
  }

  /**
   * Transform path from root-relative to module-relative
   * Example: "./modules/envir-fetch" → "../envir-fetch"
   */
  private _transformPathToModuleRelative(modulePath: string): string {
    // Remove "./modules/" prefix and add "../" prefix
    // modulePath is like "src/modules/envir-fetch"
    const pathParts = modulePath.split('/');
    const moduleName = pathParts[pathParts.length - 1]; // Get last part
    return `../${moduleName}/index.ts`;
  }

  /**
   * Get reference path for TypeScript project references
   * Example: "src/modules/envir-fetch" → "../envir-fetch"
   */
  private _getModuleReferencePath(modulePath: string): string {
    const pathParts = modulePath.split('/');
    const moduleName = pathParts[pathParts.length - 1];
    return `../${moduleName}`;
  }
}