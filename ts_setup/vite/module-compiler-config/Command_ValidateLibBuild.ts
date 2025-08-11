import { ModuleRegistry } from '../module-registry'
import { RegistryValidationBuilder, ValidationResultPrinter } from '../module-registry-validator'

export declare namespace Command_ValidateLibBuild {
  export interface Input {
    registry: ModuleRegistry;
    moduleName: string;
    options: {
      strictValidation: boolean;
      failOnValidationErrors: boolean;
      failOnCircularDeps: boolean;
      failOnMissingModules: boolean;
      failOnUnusedDeps: boolean;
    };
  }

  export interface Result {
    // void - throws on validation failure, returns normally if valid
  }
}

export class Command_ValidateLibBuild {
  execute(input: Command_ValidateLibBuild.Input): Command_ValidateLibBuild.Result {
    const { registry, moduleName, options } = input;

    // Use the RegistryValidationBuilder instead of the old visitor
    const validationBuilder = new RegistryValidationBuilder();

    const result = validationBuilder.build(registry, moduleName, {
      strictPerModuleValidation: options.strictValidation
    });

    if (result.isCorrupted) {
      const printer = new ValidationResultPrinter();
      const report = printer.print(result);
      throw new Error(`💥 REGISTRY CORRUPTED:\n${report}`);
    }

    if (!result.isValid) {
      const printer = new ValidationResultPrinter();
      const report = printer.print(result);

      // Check if we should fail based on options
      const shouldFail = (
        (options.failOnValidationErrors && result.errors.length > 0) ||
        (options.failOnCircularDeps && result.errors.some(e => e.type === 'circular_dependency')) ||
        (options.failOnMissingModules && result.errors.some(e => e.type === 'missing_internal' || e.type === 'missing_external')) ||
        (options.failOnUnusedDeps && result.errors.some(e => e.type === 'unused_external' || e.type === 'unused_internal'))
      );

      if (shouldFail) {
        const errorCount = result.errors.length;
        const warningCount = result.warnings.length;
        throw new Error(`💥 Registry validation failed: ${errorCount} error(s), ${warningCount} warning(s)\n${report}`);
      }
    }

    // Validation passed or only warnings
    if (result.warnings.length > 0) {
      const printer = new ValidationResultPrinter();
      const report = printer.print(result);
      console.warn(`⚠️ Registry validation passed with ${result.warnings.length} warning(s)\n${report}`);
    }

    console.log(`✅ Registry validation passed`);
    return {};
  }
}