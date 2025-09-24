import { ModuleRegistry } from '../module-registry';

import { TSConfigGeneratorOptions, TSConfigOutput } from "./gen-tsconfig-types";
import { Command_WriteTSConfig } from "./Command_WriteTSConfig"
import { Command_GenerateModuleTSConfigs } from './Command_GenerateModuleTSConfigs';


export class TSConfigBuilder {
  private options: Required<TSConfigGeneratorOptions>;

  constructor(options: TSConfigGeneratorOptions = {}) {
    this.options = {
      outputPath: 'tsconfig.gen.json',
      baseUrl: '.',
      includeExternalDeps: false,
      compilerOptions: {},
      rootPath: '.',
      ...options
    };
  }

  build(registry: ModuleRegistry, dryRun: boolean = false): {
    moduleName: string;
    outputPath: string;
    tsConfig: TSConfigOutput;
  }[] {
    console.log('📝 Generating TSConfig path mappings...');

    try {
      // Command pipeline orchestration
      const generateTSConfigCmd = new Command_GenerateModuleTSConfigs();
      const writeTSConfigCmd = new Command_WriteTSConfig();
      

      // Step 1: Generate TSConfig structure
      const { moduleConfigs } = generateTSConfigCmd.execute({
        registry,
        
      });


      // Step 2: Write TSConfig file
      if(!dryRun) {
        for(const moduleConfig of moduleConfigs) {
          writeTSConfigCmd.execute({
            rootPath: this.options.rootPath,
            tsConfig: moduleConfig.tsConfig,
            options: {
              ...this.options,
              outputPath: moduleConfig.outputPath
            }
          });

          console.log(`✅ TSConfig generated: ${moduleConfig.outputPath}`);
        }
      }

      return moduleConfigs;
    } catch (error) {
      console.error('❌ Failed to generate TSConfig:', error);
      throw error;
    }
  }
}