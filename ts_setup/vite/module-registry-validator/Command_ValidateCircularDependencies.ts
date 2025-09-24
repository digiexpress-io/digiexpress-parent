import { ModuleRegistry, ValidationError } from "../module-registry";

export declare namespace Command_ValidateCircularDependencies {
  export interface Input {
    registry: ModuleRegistry;
    modulesToValidate: string[];
  }

  export interface Result {
    errors: ValidationError[];
  }
}

export class Command_ValidateCircularDependencies {
  execute(input: Command_ValidateCircularDependencies.Input): Command_ValidateCircularDependencies.Result {
    const { registry, modulesToValidate } = input;

    try {
      const errors = _validateCircularDependencies(registry, modulesToValidate);
      return { errors };
    } catch (error) {
      // Return corruption error if circular validation fails
      const corruptionError: ValidationError = {
        type: 'circular_dependency_corruption',
        severity: 'corruption',
        moduleName: 'unknown',
        item: 'circular_dependencies',
        problem: `💥 CIRCULAR VALIDATION CORRUPTION: ${error.message}`,
        solution: `🔧 Circular dependency validation failed. Try FORCE_REGISTRY_REBUILD=true`,
        technicalDetails: error.stack
      };

      return { errors: [corruptionError] };
    }
  }
}

// Pure transformative functions
function _validateCircularDependencies(registry: ModuleRegistry, modulesToValidate: string[]): ValidationError[] {
  const errors: ValidationError[] = [];
  const circularDeps = registry.dependencyGraph.circularDependencies || [];

  // Filter circular dependencies that affect our validation scope
  for (const cycle of circularDeps) {
    const affectsScope = cycle.some(module => modulesToValidate.includes(module));

    if (affectsScope) {
      errors.push({
        type: 'circular_dependency',
        severity: 'error',
        moduleName: cycle[0], // First module in cycle
        item: cycle.join(' → '),
        problem: 'Circular dependency detected in module chain',
        solution: `Refactor modules to break circular dependency: ${cycle.join(' → ')}`
      });
    }
  }

  return errors;
}