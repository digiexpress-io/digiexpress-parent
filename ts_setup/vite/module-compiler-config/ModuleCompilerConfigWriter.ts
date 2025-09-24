import { resolve } from 'node:path';
import { Command_SaveLibConfigTrace } from "./Command_SaveLibConfigTrace";

import { ModuleCompilerConfig } from "./module-compiler-config-types";


export class ModuleCompilerConfigWriter {
  private options: ModuleCompilerConfig;

  constructor(options: ModuleCompilerConfig) {
    this.options = options
  }

  build(rootPath: string = process.cwd()): void {
    const fullTracePath = resolve(rootPath, this.options.build.outDir, "trace.json");
    new Command_SaveLibConfigTrace().execute({ 
      fullTracePath, config: this.options
    });

  }
}
