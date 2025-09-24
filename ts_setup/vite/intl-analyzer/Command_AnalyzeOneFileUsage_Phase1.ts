

export declare namespace Command_AnalyzeOneFileUsage_Phase1 {
  export interface Input {
    fileContent: string, 
    translationKeys: Set<string>,
    filePath: string
  }
  export type Result = Map<string, Array<{ file: string; line: number; column: number }>>;
}

export class Command_AnalyzeOneFileUsage_Phase1 {
  execute(input: Command_AnalyzeOneFileUsage_Phase1.Input): Command_AnalyzeOneFileUsage_Phase1.Result {
  
    const { fileContent, translationKeys, filePath } = input;
    const usageMap = new Map<string, Array<{ file: string; line: number; column: number }>>();
    
    // Initialize empty arrays
    for (const key of translationKeys) {
      usageMap.set(key, []);
    }
    
    // Intl usage patterns to search for:
    const patterns = [
      /formatMessage\s*\(\s*\{\s*id:\s*['"`]([^'"`]+)['"`]/g,           // formatMessage({ id: "key" })
      /<FormattedMessage\s+id=['"`]([^'"`]+)['"`]/g,                   // <FormattedMessage id="key" />
      /intl\.formatMessage\s*\(\s*\{\s*id:\s*['"`]([^'"`]+)['"`]/g,    // intl.formatMessage({ id: "key" })
      /useIntl\(\)\.formatMessage\s*\(\s*\{\s*id:\s*['"`]([^'"`]+)['"`]/g, // useIntl().formatMessage({ id: "key" })
      /t\s*\(\s*['"`]([^'"`]+)['"`]/g,                                 // t("key") - common i18n pattern
    ];
    
    const lines = fileContent.split('\n');
    
    for (let lineIndex = 0; lineIndex < lines.length; lineIndex++) {
      const line = lines[lineIndex];
      const lineNumber = lineIndex + 1;
      
      for (const pattern of patterns) {
        let match;
        while ((match = pattern.exec(line)) !== null) {
          const foundKey = match[1];
          
          if (translationKeys.has(foundKey)) {
            const column = match.index || 0;
            
            const existing = usageMap.get(foundKey) || [];
            existing.push({
              file: filePath,
              line: lineNumber,
              column: column + 1 // 1-based indexing
            });
            usageMap.set(foundKey, existing);
          }
        }
        // Reset regex lastIndex to avoid issues with global flag
        pattern.lastIndex = 0;
      }
    }
    
    return usageMap;
  }
}