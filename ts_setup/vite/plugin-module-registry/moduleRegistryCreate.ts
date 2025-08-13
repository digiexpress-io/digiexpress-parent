import { Plugin } from 'vite'
import { ModuleRegistryCacheBuilder } from '../module-registry-cache';
import { TSConfigBuilder } from '../gen-tsconfig'
import { RegistryValidationBuilder, ValidationResultPrinter } from '../module-registry-validator';


export function moduleRegistryCreate(props: { strict: boolean }): Plugin {
  return {
    name: 'module-registry-create',

    config() {
      const rootPath = process.cwd();
      const registry = new ModuleRegistryCacheBuilder({
        onRegistryBuilt(registry) {
          new TSConfigBuilder({ rootPath }).build(registry);
        },
      }).build(rootPath, true);

      if(!props.strict) {
        return;
      }

      // Use the RegistryValidationBuilder instead of the old visitor
      const validationBuilder = new RegistryValidationBuilder();

      for (const moduleName of Object.keys(registry.modules)) {
        const result = validationBuilder.build(registry, moduleName, {
          strictPerModuleValidation: true
        });
        if (result.isCorrupted) {
          const printer = new ValidationResultPrinter();
          const report = printer.print(result);
          throw new Error(`💥 REGISTRY CORRUPTED:\n${report}`);
        }

        if (!result.isValid) {
          const printer = new ValidationResultPrinter();
          const report = printer.print(result);
          const errorCount = result.errors.length;
          const warningCount = result.warnings.length;
          throw new Error(`💥 Registry validation failed: ${errorCount} error(s), ${warningCount} warning(s)\n${report}`);
        }

        // Validation passed or only warnings
        if (result.warnings.length > 0) {
          const printer = new ValidationResultPrinter();
          const report = printer.print(result);
          console.warn(`⚠️ Registry validation passed with ${result.warnings.length} warning(s)\n${report}`);
        }
      }
    }
  };
}


