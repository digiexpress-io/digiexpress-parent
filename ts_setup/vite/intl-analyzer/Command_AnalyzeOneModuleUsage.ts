
import path from "node:path";

import { ModuleInfo } from "../module-registry";
import { ModuleUsage } from "./intl-types";
import { FileCache } from './FileCache';

export declare namespace Command_AnalyzeOneModuleUsage {

  export interface Command {    
    execute(input: {
      fileContent: string, 
      translationKeys: Set<string>,
      filePath: string
    }): Map<string, Array<{ file: string; line: number; column: number }>>
  }

  export interface Input {
    fileCache: FileCache,
    moduleName: string,
    moduleInfo: ModuleInfo,
    translationKeys: Set<string>,
    rootPath: string,
    command: Command
  }
  export type Result = Map<string, ModuleUsage[]>;
}


export class Command_AnalyzeOneModuleUsage {
  async execute(input: Command_AnalyzeOneModuleUsage.Input): Promise<Command_AnalyzeOneModuleUsage.Result> {
    const { moduleName, moduleInfo, translationKeys, rootPath } = input;
    const moduleUsageMap = new Map<string, ModuleUsage[]>();
    
    // Initialize empty arrays for all keys
    for (const key of translationKeys) {
      moduleUsageMap.set(key, []);
    }
    
    try {
      const moduleFiles = await input.fileCache.readFiles(path.join(input.rootPath, moduleInfo.path));
      
      for (const moduleFile of moduleFiles) {
        const fileUsage = this.scanFileForIntlKeys(moduleFile.path, moduleFile.content, translationKeys, input.command);
        
        for (const [key, locations] of fileUsage.entries()) {
          if (locations.length > 0) {
            const existing = moduleUsageMap.get(key) || [];
            
            // Create or update module usage
            const moduleUsage: ModuleUsage = {
              moduleName,
              folders: _extractFoldersFromFile(moduleFile.path),
              usageLocations: locations
            };
            
            moduleUsageMap.set(key, [...existing, moduleUsage]);
          }
        }
      }
      
    } catch (error) {
      console.warn(`⚠️  Failed to scan module ${moduleName}:`, error);
    }
    
    return moduleUsageMap;
  }
  
  private scanFileForIntlKeys(
    filePath: string,
    fileContent: string,
    translationKeys: Set<string>,
    command: Command_AnalyzeOneModuleUsage.Command
  ): Map<string, Array<{ file: string; line: number; column: number }>> {
    const fileUsageMap = new Map<string, Array<{ file: string; line: number; column: number }>>();
    
    // Initialize empty arrays
    for (const key of translationKeys) {
      fileUsageMap.set(key, []);
    }
    
    try {
      const usage = command.execute({ fileContent, translationKeys, filePath });
      
      for (const [key, locations] of usage.entries()) {
        fileUsageMap.set(key, locations);
      }
      
    } catch (error) {
      console.warn(`⚠️  Failed to scan file ${filePath}:`, error);
    }
    
    return fileUsageMap;
  }
}

function _extractFoldersFromFile(filePath: string): string[] {
  // Extract folder path from file path
  const parts = filePath.split('/');
  const folders: string[] = [];
  
  let currentPath = '';
  for (let i = 0; i < parts.length - 1; i++) { // Exclude filename
    if (currentPath) {
      currentPath += '/' + parts[i];
    } else {
      currentPath = parts[i];
    }
    folders.push(currentPath);
  }
  return folders;
}
