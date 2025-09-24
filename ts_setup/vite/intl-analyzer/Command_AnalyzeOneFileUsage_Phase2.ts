export declare namespace Command_AnalyzeOneFileUsage_Phase2 {
  export interface Input {
    fileContent: string,
    translationKeys: Set<string>,
    filePath: string
  }
  export type Result = Map<string, Array<{ file: string; line: number; column: number }>>;
}

export class Command_AnalyzeOneFileUsage_Phase2 {
  execute(input: Command_AnalyzeOneFileUsage_Phase2.Input): Command_AnalyzeOneFileUsage_Phase2.Result {
    const { fileContent, translationKeys, filePath } = input;
    const usageMap = new Map<string, Array<{ file: string; line: number; column: number }>>();
    
    // Initialize empty arrays for all keys
    for (const key of translationKeys) {
      usageMap.set(key, []);
    }
    
    const lines = fileContent.split('\n');
    
    // Phase 2: Brute force search for quoted strings
    for (const translationKey of translationKeys) {
      // Create quoted variations of the key
      const quotedVariations = [
        `"${translationKey}"`,  // Double quotes
        `'${translationKey}'`,  // Single quotes
        `\`${translationKey}\`` // Backticks
      ];
      
      // Search each line for any quoted variation
      for (let lineIndex = 0; lineIndex < lines.length; lineIndex++) {
        const line = lines[lineIndex];
        const lineNumber = lineIndex + 1;
        
        for (const quotedKey of quotedVariations) {
          let searchIndex = 0;
          let foundIndex = -1;
          
          // Find all occurrences of this quoted key in the line
          while ((foundIndex = line.indexOf(quotedKey, searchIndex)) !== -1) {
            const existing = usageMap.get(translationKey) || [];
            existing.push({
              file: filePath,
              line: lineNumber,
              column: foundIndex + 1 // 1-based indexing
            });

            usageMap.set(translationKey, existing);
            
            // Move search index past this match to find additional occurrences
            searchIndex = foundIndex + quotedKey.length;
          }
        }
      }
    }
    
    return usageMap;
  }
}