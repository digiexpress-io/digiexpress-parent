import { ModuleInfo, ModuleRegistry, ValidationError, ValidationOptions } from "../module-registry";

export declare namespace Command_ValidateModules {
  export interface Input {
    registry: ModuleRegistry;
    modulesToValidate: string[];
    options: ValidationOptions;
  }

  export interface Result {
    errors: ValidationError[];
    warnings: ValidationError[];
    corruptions: ValidationError[];
  }
}

export class Command_ValidateModules {
  execute(input: Command_ValidateModules.Input): Command_ValidateModules.Result {

    const { registry, modulesToValidate, options } = input;

    const errors: ValidationError[] = [];
    const warnings: ValidationError[] = [];
    const corruptions: ValidationError[] = [];

    // Validate each module in scope
    for (const moduleName of modulesToValidate) {
      try {
        const moduleErrors = _validateSingleModule(registry, moduleName, modulesToValidate, options);

        for (const error of moduleErrors) {
          if (error.severity === 'corruption') {
            corruptions.push(error);
          } else if (error.severity === 'error') {
            errors.push(error);
          } else {
            warnings.push(error);
          }
        }

      } catch (error: any) {
        // Module validation corrupted - add corruption error but continue
        corruptions.push({
          type: 'corruption_error',
          severity: 'corruption',
          moduleName,
          item: moduleName,
          problem: `💥 MODULE VALIDATION CORRUPTION: ${error.message}`,
          solution: `🔧 Module '${moduleName}' may be corrupted. Try FORCE_REGISTRY_REBUILD=true`,
          technicalDetails: error.stack
        });
      }
    }

    return { errors, warnings, corruptions };
  }
}

// Pure transformative functions
function _validateSingleModule(registry: ModuleRegistry, moduleName: string, validationScope: string[], options: ValidationOptions): ValidationError[] {
  const errors: ValidationError[] = [];

  try {
    const moduleInfo = registry.modules[moduleName];

    if (!moduleInfo) {
      errors.push({
        type: 'missing_module',
        severity: 'error',
        moduleName,
        item: moduleName,
        problem: 'Module exists in dependency graph but missing from registry.modules',
        solution: `Add module '${moduleName}' to the registry or remove references to it`
      });
      return errors;
    }

    // Validate module structure
    _validateModuleStructure(moduleInfo, errors);
    _validateDependencyReferences(registry, moduleInfo, errors);

    // Validate dependencies based on validation mode
    if (options.strictPerModuleValidation) {
      // STRICT MODE: Each module must have all its dependencies declared in its own package.json
      _validateModuleDependenciesStrict(registry, moduleInfo, errors);
    } else {
      // AGGREGATE MODE: Dependencies can be satisfied anywhere in the validation scope
      _validateModuleDependenciesAggregate(registry, moduleInfo, validationScope, errors);
    }

  } catch (error: any) {
    errors.push({
      type: 'invalid_structure',
      severity: 'corruption',
      moduleName,
      item: moduleName,
      problem: `💥 MODULE STRUCTURE CORRUPTION: ${error.message}`,
      solution: `🔧 Module '${moduleName}' structure is corrupted. Rebuild registry.`,
      technicalDetails: error.stack
    });
  }

  return errors;
}



function _getMissingRefs(registry: ModuleRegistry, moduleInfo: ModuleInfo, collector: string[], scanned: string[]): void {


  for (const internalDepName of moduleInfo.actualInternalDependencies) {
    if(internalDepName === moduleInfo.name) {
      continue; // lets simply not crash... self ref should be error
    }
    if(scanned.includes(internalDepName)) {
      continue;
    }

    scanned.push(internalDepName);

    const scopeModuleInfo = registry.modules[internalDepName];
    if (scopeModuleInfo) {
      scopeModuleInfo.actualInternalDependencies;
    }
  }
}

class CollectDepChain {
  private _collector: string[] = [];
  private _refs: string[] = [];
  private _scanned: string[] = [];
  private _registry: ModuleRegistry;
  private _moduleInfo: ModuleInfo;

  constructor(registry: ModuleRegistry, moduleInfo: ModuleInfo) {
    this._registry = registry;
    this._moduleInfo = moduleInfo;
  }

  findAll(): string[] {
    const isLib = this._moduleInfo.path.startsWith('modules/lib-');
    const isDemoApp = this._moduleInfo.path.startsWith('modules/demo-app-');
    const isValidTarget = isLib || isDemoApp;

    if(!(isValidTarget)) {
      return [];
    }

    this.scanModule(this._moduleInfo);
    const result: string[] = [];

    for(const ref of this._refs) {
      if(result.includes(ref)) {
        continue;
      }
      if(this._moduleInfo.missingDependencies.includes(ref)) {
        continue;
      }
      if(this._moduleInfo.internalDependencies.includes(ref)) {
        continue;
      }
      result.push(ref);
    }

    return result;
  }

  private scanModule(moduleInfo: ModuleInfo) {
    this._scanned.push(moduleInfo.name);

    for (const internalDepName of moduleInfo.actualInternalDependencies) {
      if(internalDepName === moduleInfo.name) {
        continue; // lets simply not crash... self ref should be error
      }
      if(this._scanned.includes(internalDepName)) {
        continue;
      }
      this._refs.push(internalDepName);

      const next: ModuleInfo = this._registry.modules[internalDepName];
      if(next) {
        this.scanModule(next);
      }
    }
  }
}

function _validateModuleDependenciesStrict(registry: ModuleRegistry, moduleInfo: ModuleInfo, errors: ValidationError[]): void {
  console.log(`🔒 Strict validation for ${moduleInfo.name} - each module must be self-sufficient`);

  const refs: string[] = new CollectDepChain(registry, moduleInfo).findAll();
  const missingDependencies: string[] = [... (moduleInfo.missingDependencies ?? []), ...refs];

  // Validate missing external dependencies
  for (const missingDep of missingDependencies) {
    const isExternal = !missingDep.startsWith('@dxs-ts/');

    errors.push({
      type: isExternal ? 'missing_external' : 'missing_internal',
      severity: 'error',
      moduleName: moduleInfo.name,
      item: missingDep,
      problem: `Used in source code but not declared in package.json`,
      solution: `Add "${missingDep}" to dependencies in ${moduleInfo.name}/package.json`
    });
  }


  // Validate unused dependencies (as warnings in strict mode)
  if (moduleInfo.unusedDependencies) {
    for (const unusedDep of moduleInfo.unusedDependencies) {
      const isExternal = !unusedDep.startsWith('@dxs-ts/');
      const isInternal = unusedDep.startsWith('@dxs-ts/');

      errors.push({
        type: isExternal ? 'unused_external' : 'unused_internal',
        severity: 'warning',
        moduleName: moduleInfo.name,
        item: unusedDep,
        problem: `Declared in package.json but not used in source code`,
        solution: `Remove "${unusedDep}" from dependencies in ${moduleInfo.name}/package.json or use it in your code`
      });
    }
  }

  const missingCount = moduleInfo.missingDependencies?.length || 0;
  const unusedCount = moduleInfo.unusedDependencies?.length || 0;

  if (missingCount > 0 || unusedCount > 0) {
    console.log(`   🔒 ${moduleInfo.name}: ${missingCount} missing, ${unusedCount} unused dependencies`);
  } else {
    console.log(`   ✅ ${moduleInfo.name}: All dependencies properly declared`);
  }
}

function _validateModuleDependenciesAggregate(registry: ModuleRegistry, moduleInfo: ModuleInfo, validationScope: string[], errors: ValidationError[]): void {
  console.log(`🌐 Aggregate validation for ${moduleInfo.name} - dependencies can be shared in scope`);

  // Collect all declared dependencies available in the scope
  const scopeExternalDeps = new Set<string>();
  const scopeInternalDeps = new Set<string>();

  for (const scopeModuleName of validationScope) {
    const scopeModuleInfo = registry.modules[scopeModuleName];
    if (scopeModuleInfo) {
      // Add declared external dependencies
      scopeModuleInfo.externalDependencies?.forEach(dep => scopeExternalDeps.add(dep));
      // Add declared internal dependencies  
      scopeModuleInfo.internalDependencies?.forEach(dep => scopeInternalDeps.add(dep));
    }
  }

  // Check if any actual dependencies are completely missing from scope
  const actualExternalDeps = moduleInfo.actualExternalDependencies || [];
  const actualInternalDeps = moduleInfo.actualInternalDependencies || [];

  // Validate external dependencies
  for (const actualDep of actualExternalDeps) {
    if (!scopeExternalDeps.has(actualDep)) {
      errors.push({
        type: 'missing_external',
        severity: 'error',
        moduleName: moduleInfo.name,
        item: actualDep,
        problem: `External dependency used but not declared anywhere in validation scope`,
        solution: `Add "${actualDep}" to dependencies module within the scope: [${moduleInfo.name}]`
      });
    }
  }

  // Validate internal dependencies
  for (const actualDep of actualInternalDeps) {
    if (!scopeInternalDeps.has(actualDep)) {
      errors.push({
        type: 'missing_internal',
        severity: 'error',
        moduleName: moduleInfo.name,
        item: actualDep,
        problem: `Internal dependency used but not declared anywhere in validation scope`,
        solution: `Add "${actualDep}" to dependencies in any module within the scope: [${validationScope.join(', ')}]`
      });
    }
  }

  const missingExternal = actualExternalDeps.filter(dep => !scopeExternalDeps.has(dep));
  const missingInternal = actualInternalDeps.filter(dep => !scopeInternalDeps.has(dep));

  if (missingExternal.length > 0 || missingInternal.length > 0) {
    console.log(`   🌐 ${moduleInfo.name}: ${missingExternal.length} missing external, ${missingInternal.length} missing internal (in scope)`);
  } else {
    console.log(`   ✅ ${moduleInfo.name}: All dependencies satisfied within scope`);
  }
}

function _validateModuleStructure(moduleInfo: any, errors: ValidationError[]): void {
  const requiredFields = ['name', 'path', 'dependencies', 'internalDependencies', 'externalDependencies'];

  for (const field of requiredFields) {
    if (!(field in moduleInfo)) {
      errors.push({
        type: 'invalid_structure',
        severity: 'error',
        moduleName: moduleInfo.name || 'unknown',
        item: field,
        problem: `Required field '${field}' is missing from module info`,
        solution: `Rebuild the registry to restore missing field '${field}'`
      });
    }
  }
}

function _validateDependencyReferences(registry: ModuleRegistry, moduleInfo: any, errors: ValidationError[]): void {
  // Check that internal dependencies actually exist in registry
  const internalDeps = moduleInfo.internalDependencies || [];

  for (const dep of internalDeps) {
    if (!registry.modules[dep]) {
      errors.push({
        type: 'missing_internal',
        severity: 'error',
        moduleName: moduleInfo.name,
        item: dep,
        problem: `Internal dependency '${dep}' not found in registry`,
        solution: `Add module '${dep}' to the registry or remove it from ${moduleInfo.name}'s dependencies`
      });
    }
  }
}