import { Command_ProcessDTSImports } from './Command_ProcessDTSImports'

export interface DTSBuilderOptions {
  tsconfigPath?: string;
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

  constructor(options: DTSBuilderOptions = {}) {
    this.options = {
      tsconfigPath: './tsconfig.json',
      bundleTypes: true,
      processImports: true,
      ...options
    };
  }

  build(): DTSBuildConfig {
    const processImportsCmd = new Command_ProcessDTSImports();

    const config: DTSBuildConfig = {
      tsconfigPath: this.options.tsconfigPath,
      bundleTypes: this.options.bundleTypes
    };

    // Add import processing if enabled
    if (this.options.processImports) {
      config.beforeWriteFile = (filePath: string, content: string) => {
        return processImportsCmd.execute({ filePath, content });
      };
    }

    return config;
  }
}
