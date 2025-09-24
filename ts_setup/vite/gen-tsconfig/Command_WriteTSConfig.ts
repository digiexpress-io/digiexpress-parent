import { writeFileSync, mkdirSync, existsSync, readFileSync } from 'node:fs';
import { resolve, dirname } from 'node:path';
import { TSConfigGeneratorOptions, TSConfigOutput } from './gen-tsconfig-types'



export declare namespace Command_WriteTSConfig {
  export interface Input {
    rootPath: string;
    tsConfig: TSConfigOutput;
    options: Required<TSConfigGeneratorOptions>;
  }

  export interface Result {
    // void - writes file to disk
  }
}

export class Command_WriteTSConfig {
  execute(input: Command_WriteTSConfig.Input): Command_WriteTSConfig.Result {
    const { rootPath, tsConfig, options } = input;

    const fullOutputPath = resolve(rootPath, options.outputPath);
    const outputDir = dirname(fullOutputPath);

    // Ensure output directory exists
    if (existsSync(outputDir)) {
      try {
        const previous = _diffable(readFileSync(fullOutputPath, 'utf-8'));
        const next = _diffable(JSON.stringify(tsConfig, null, 2));
        if(previous === next) {
          console.log(` ✅ tsconfig: ${input.rootPath} up to date`);
          return {};
        }
      } catch(e) {
        // probably file does't exists, everything fine, generate new ...
      }
    } else {
      mkdirSync(outputDir, { recursive: true });
    }

    // Write formatted JSON
    const content = JSON.stringify(tsConfig, null, 2);
    writeFileSync(fullOutputPath, content, 'utf-8');

    return {};
  }
}


function _diffable(content: string): string {
  const raw: Partial<TSConfigOutput> = JSON.parse(content);
  delete raw['_generated'];
  return JSON.stringify(raw);
}