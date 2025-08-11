import { existsSync, readFileSync } from "node:fs";
import { VersionsFile } from "../module-registry";

export declare namespace Command_LoadVersionsFile {
  export interface Input {
    versionsFilePath?: string;
  }
  
  export interface Result {
    versionsFile: VersionsFile;
    fileExists: boolean;
  }
}

export class Command_LoadVersionsFile {
  execute(input: Command_LoadVersionsFile.Input): Command_LoadVersionsFile.Result {
    const { versionsFilePath = './.modules/versions.json' } = input;
    
    console.log(`📋 Loading versions file: ${versionsFilePath}`);
    
    if (!existsSync(versionsFilePath)) {
      console.log(`📂 Versions file not found, will create new one`);
      return { versionsFile: {}, fileExists: false };
    }
    
    try {
      const content = readFileSync(versionsFilePath, 'utf-8');
      const versionsFile = JSON.parse(content);
      
      console.log(`✅ Loaded ${Object.keys(versionsFile).length} version entries`);
      return { versionsFile, fileExists: true };
      
    } catch (error) {
      console.warn(`⚠️  Failed to parse versions file: ${error}`);
      return { versionsFile: {}, fileExists: false };
    }
  }
}