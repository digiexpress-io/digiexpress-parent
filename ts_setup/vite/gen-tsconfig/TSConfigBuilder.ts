import { ModuleRegistry } from '../module-registry';

import { TSConfigGeneratorOptions, TSConfigOutput } from "./gen-tsconfig-types";
import { Command_GenerateTSConfig } from "./Command_GenerateTSConfig"
import { Command_WriteTSConfig } from "./Command_WriteTSConfig"


export class TSConfigBuilder {
  private options: Required<TSConfigGeneratorOptions>;

  constructor(options: TSConfigGeneratorOptions = {}) {
    this.options = {
      outputPath: 'tsconfig.gen.json',
      baseUrl: '.',
      includeExternalDeps: false,
      compilerOptions: {},
      ...options
    };
  }

  build(registry: ModuleRegistry, dryRun: boolean = false): TSConfigOutput {
    console.log('📝 Generating TSConfig path mappings...');

    try {
      // Command pipeline orchestration
      const generateTSConfigCmd = new Command_GenerateTSConfig();
      const writeTSConfigCmd = new Command_WriteTSConfig();

      // Step 1: Generate TSConfig structure
      const { tsConfig } = generateTSConfigCmd.execute({
        registry,
        options: this.options
      });

      // Step 2: Write TSConfig file
      if(!dryRun) {
        writeTSConfigCmd.execute({
          rootPath: registry.rootPath,
          tsConfig,
          options: this.options
        });
      }

      console.log(`✅ TSConfig generated: ${this.options.outputPath}`);
      console.log(`   📍 ${Object.keys(tsConfig.compilerOptions.paths).length} path mappings created`);

      return tsConfig;
    } catch (error) {
      console.error('❌ Failed to generate TSConfig:', error);
      throw error;
    }
  }
}