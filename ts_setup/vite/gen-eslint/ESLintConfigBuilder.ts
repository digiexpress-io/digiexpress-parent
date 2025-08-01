import { ESLintConfigGeneratorOptions } from "./gen-eslint-types";
import { ModuleRegistry } from '../module-registry';
import { Command_GenerateModuleConfigs } from './Command_GenerateModuleConfigs'
import { Command_WriteConfigFiles } from './Command_WriteConfigFiles'


export class ESLintConfigBuilder {
  private options: Required<ESLintConfigGeneratorOptions>;

  constructor(options: ESLintConfigGeneratorOptions = {}) {
    this.options = {
      outputDir: '.modules/eslint',
      includeExternalDeps: true,
      allowRelativeImports: true,
      severity: 'error',
      ...options
    };
  }

  build(registry: ModuleRegistry): void {
    console.log('📝 Generating ESLint configurations for modules...');

    try {
      // Command pipeline orchestration
      const generateModuleConfigsCmd = new Command_GenerateModuleConfigs();
      const writeConfigFilesCmd = new Command_WriteConfigFiles();

      // Step 1: Generate module configurations
      const { moduleConfigs } = generateModuleConfigsCmd.execute({
        registry,
        options: this.options
      });

      // Step 2: Write configuration files
      writeConfigFilesCmd.execute({
        rootPath: registry.rootPath,
        moduleConfigs,
        options: this.options
      });

      console.log(`✅ ESLint configs generated: ${this.options.outputDir}`);
      console.log(`   📍 ${moduleConfigs.length} module configurations created`);

    } catch (error) {
      console.error('❌ Failed to generate ESLint configs:', error);
      throw error;
    }
  }
}