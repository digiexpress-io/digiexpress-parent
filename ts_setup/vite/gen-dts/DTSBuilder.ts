import { ModuleRegistry } from '../module-registry';
import { ModuleRegistryCacheBuilder } from '../module-registry-cache';
import { Command_ProcessDTSImports } from './Command_ProcessDTSImports'

export interface DTSBuilderOptions {
  registry?: ModuleRegistry | undefined
  moduleName: string;
  bundleTypes?: boolean;
  processImports?: boolean;
}

export interface DTSBuildConfig {
  tsconfigPath: string;
  bundleTypes: boolean;
  beforeWriteFile?: (filePath: string, content: string) => { filePath: string; content: string };
}

export class DTSBuilder {
  private options: Required<DTSBuilderOptions>;

  constructor(options: DTSBuilderOptions) {
    const rootPath = process.cwd();
    const registry =( options.registry ?? new ModuleRegistryCacheBuilder().build(rootPath, false));
    this.options = {
      bundleTypes: true,
      processImports: true,
      ...options,
      registry
    };
  }

  build(): DTSBuildConfig {
    const processImportsCmd = new Command_ProcessDTSImports();

    const module = this.options.registry.modules[this.options.moduleName];


    const config: DTSBuildConfig = {
      tsconfigPath: "./" + module.path + '/tsconfig.json',
      bundleTypes: this.options.bundleTypes
    };

    // Add import processing if enabled
    if (this.options.processImports) {
      config.beforeWriteFile = (filePath: string, content: string) => {
        return processImportsCmd.execute({ filePath, content });
      };
    }

    console.log(config);
    //throw Error('wooops');
    return config;
  }
}
