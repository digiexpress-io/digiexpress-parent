import { dirname } from "node:path";
import { existsSync, mkdirSync, writeFileSync } from "node:fs";

import { VersionsFile } from "../module-registry";


export declare namespace Command_SaveVersionsFile {
  export interface Input {
    versionsFile: VersionsFile;
    versionsFilePath?: string;
  }
  
  export interface Result {
    savedPath: string;
  }
}

export class Command_SaveVersionsFile {
  execute(input: Command_SaveVersionsFile.Input): Command_SaveVersionsFile.Result {
    const { versionsFile, versionsFilePath = './.modules/versions.json' } = input;
    
    console.log(`💾 Saving versions file: ${versionsFilePath}`);
    
    // Ensure directory exists
    const dir = dirname(versionsFilePath);
    if (!existsSync(dir)) {
      mkdirSync(dir, { recursive: true });
    }
    
    writeFileSync(versionsFilePath, JSON.stringify(versionsFile, null, 2), 'utf-8');
    
    console.log(`✅ Versions file saved with ${Object.keys(versionsFile).length} entries`);
    
    return { savedPath: versionsFilePath };
  }
}