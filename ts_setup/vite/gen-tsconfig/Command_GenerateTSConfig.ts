import { ModuleRegistry } from '../module-registry'
import { TSConfigGeneratorOptions, TSConfigOutput } from './gen-tsconfig-types'


export declare namespace Command_GenerateTSConfig {
  export interface Input {
    registry: ModuleRegistry;
    options: Required<TSConfigGeneratorOptions>;
  }

  export interface Result {
    tsConfig: TSConfigOutput;
  }
}

export class Command_GenerateTSConfig {
  execute(input: Command_GenerateTSConfig.Input): Command_GenerateTSConfig.Result {
    const { registry, options } = input;

    const paths: Record<string, string[]> = {};

    // hardcoded paths, material ui to emotion hickups
    paths['@emotion/styled'] = ['node_modules/@emotion/styled']


    // Generate path mappings for all modules
    for (const [moduleName, moduleInfo] of Object.entries(registry.modules)) {
      // Map module name to its index.ts file
      const indexPath = `./${moduleInfo.path}/index.ts`;
      paths[moduleName] = [indexPath];

      console.log(`   📦 ${moduleName} → ${indexPath}`);
    }

    // Optionally include external dependencies (usually not needed for path mapping)
    if (options.includeExternalDeps) {
      const externalDeps = _getAllExternalDependencies(registry);

      for (const dep of externalDeps) {
        // Map to node_modules (though TypeScript usually resolves these automatically)
        paths[dep] = [`./node_modules/${dep}`];
        console.log(`   🔗 ${dep} → ./node_modules/${dep}`);
      }
    }

    // Build complete tsconfig structure
    const tsConfig: TSConfigOutput = {
      compilerOptions: {
        baseUrl: options.baseUrl,
        paths,
        ...options.compilerOptions
      },
      _generated: {
        by: 'TSConfigBuilder',
        at: new Date().toISOString(),
        fromRegistry: registry.checksum.substring(0, 12),
        moduleCount: Object.keys(registry.modules).length
      }
    };

    return { tsConfig };
  }
}

// Pure transformative functions
function _getAllExternalDependencies(registry: ModuleRegistry): Set<string> {
  const externalDeps = new Set<string>();

  for (const moduleInfo of Object.values(registry.modules)) {
    moduleInfo.externalDependencies.forEach(dep => externalDeps.add(dep));
  }

  return externalDeps;
}