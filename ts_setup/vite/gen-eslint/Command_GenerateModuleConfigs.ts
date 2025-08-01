import { ModuleRegistry } from '../module-registry'
import { ESLintConfigGeneratorOptions, ESLintModuleConfig } from './gen-eslint-types';

export declare namespace Command_GenerateModuleConfigs {
  export interface Input {
    registry: ModuleRegistry;
    options: Required<ESLintConfigGeneratorOptions>;
  }

  export interface Result {
    moduleConfigs: ESLintModuleConfig[];
  }
}

export class Command_GenerateModuleConfigs {
  execute(input: Command_GenerateModuleConfigs.Input): Command_GenerateModuleConfigs.Result {
    const { registry, options } = input;

    const allInternalModules = Object.keys(registry.modules).filter(name => name.startsWith('@dxs-ts/'));
    const allExternalModules = _getAllExternalDependencies(registry);
    const moduleConfigs: ESLintModuleConfig[] = [];

    for (const [moduleName, moduleInfo] of Object.entries(registry.modules)) {
      console.log(`   🔍 Processing ${moduleName}...`);

      // Get declared dependencies from package.json
      const declaredInternal = moduleInfo.internalDependencies || [];
      const declaredExternal = moduleInfo.externalDependencies || [];

      // Calculate blocked internal imports (all internal modules except declared ones)
      const blockedInternal = allInternalModules.filter(name =>
        name !== moduleName && !declaredInternal.includes(name)
      );

      // Generate ESLint rules for this module
      const eslintRules = _generateESLintRules({
        moduleName,
        allowedInternal: declaredInternal,
        blockedInternal,
        allowedExternal: declaredExternal,
        allExternalModules,
        options
      });

      const config: ESLintModuleConfig = {
        moduleName,
        allowedInternalImports: declaredInternal,
        blockedInternalImports: blockedInternal,
        allowedExternalImports: declaredExternal,
        generatedRules: eslintRules
      };

      moduleConfigs.push(config);

      console.log(`     ✅ ${blockedInternal.length} internal imports blocked, ${declaredExternal.length} external imports allowed`);
    }

    return { moduleConfigs };
  }
}

// Pure transformative functions
function _getAllExternalDependencies(registry: ModuleRegistry): string[] {
  const allExternals = new Set<string>();

  for (const moduleInfo of Object.values(registry.modules)) {
    moduleInfo.externalDependencies?.forEach(dep => allExternals.add(dep));
  }

  return Array.from(allExternals).sort();
}

function _generateESLintRules(params: {
  moduleName: string;
  allowedInternal: string[];
  blockedInternal: string[];
  allowedExternal: string[];
  allExternalModules: string[];
  options: Required<ESLintConfigGeneratorOptions>;
}): Record<string, any> {
  const { moduleName, blockedInternal, allowedExternal, allExternalModules, options } = params;

  const rules: Record<string, any> = {};

  // Rule 1: Block undeclared internal module imports
  if (blockedInternal.length > 0) {
    rules['no-restricted-imports'] = [
      options.severity,
      {
        patterns: [
          ...blockedInternal.map(module => ({
            group: [module, `${module}/*`],
            message: `Import of '${module}' is not allowed. Add it to dependencies in ${moduleName}/package.json to use it.`
          })),
          // Block relative imports to other modules
          ...(!options.allowRelativeImports ? [{
            group: ['../*'],
            message: 'Relative imports across module boundaries are not allowed. Use the @dxs-ts/* module names instead.'
          }] : [])
        ]
      }
    ];
  }

  // Rule 2: Block undeclared external dependencies (if enabled)
  if (options.includeExternalDeps) {
    const blockedExternal = allExternalModules.filter(pkg => !allowedExternal.includes(pkg));

    if (blockedExternal.length > 0) {
      if (!rules['no-restricted-imports']) {
        rules['no-restricted-imports'] = [options.severity, { patterns: [] }];
      }

      rules['no-restricted-imports'][1].patterns.push(
        ...blockedExternal.map(pkg => ({
          group: [pkg, `${pkg}/*`],
          message: `External dependency '${pkg}' is not declared. Add it to dependencies in ${moduleName}/package.json to use it.`
        }))
      );
    }
  }

  return rules;
}