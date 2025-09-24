export declare namespace Command_AnalyzeOneFileUsage_Phase3 {
  export interface Input {
    fileContent: string,
    translationKeys: Set<string>,
    filePath: string
  }
  export type Result = Map<string, Array<{ file: string; line: number; column: number }>>;
}

export class Command_AnalyzeOneFileUsage_Phase3 {
  private knownGroups: string[];

  constructor(knownGroups: string[] = []) {
    this.knownGroups = knownGroups;
  }

  execute(input: Command_AnalyzeOneFileUsage_Phase3.Input): Command_AnalyzeOneFileUsage_Phase3.Result {
    const { fileContent, translationKeys, filePath } = input;
    const usageMap = new Map<string, Array<{ file: string; line: number; column: number }>>();
    
    // Initialize empty arrays for all keys
    for (const key of translationKeys) {
      usageMap.set(key, []);
    }
    
    // Build group patterns map for efficient lookup
    const groupPatterns = this.buildGroupPatterns(this.knownGroups, translationKeys);
    
    if (groupPatterns.size === 0) {
      return usageMap;
    }
    const lines = fileContent.split('\n');
    
    // Search for known group base patterns in the file
    for (let lineIndex = 0; lineIndex < lines.length; lineIndex++) {
      const line = lines[lineIndex];
      const lineNumber = lineIndex + 1;
      
      // For each group pattern, look for the base pattern in this line
      for (const [basePattern, matchingKeys] of groupPatterns.entries()) {
        const matches = this.findGroupPatternUsage(line, basePattern);
        
        for (const match of matches) {
          // Associate all matching keys with this usage location
          for (const matchingKey of matchingKeys) {
            const existing = usageMap.get(matchingKey) || [];
            existing.push({
              file: filePath,
              line: lineNumber,
              column: match.column
            });
            usageMap.set(matchingKey, existing);
          }
        }
      }
    }
    
    // Log findings
    const foundKeys = Array.from(usageMap.entries()).filter(([_, locations]) => locations.length > 0);
    if (foundKeys.length > 0) {
      console.log(`  ✅ Phase 3: Found ${foundKeys.length} keys via group patterns`);
    }
    
    return usageMap;
  }
  
  private buildGroupPatterns(knownGroups: string[], translationKeys: Set<string>): Map<string, string[]> {
    const groupPatterns = new Map<string, string[]>();
    
    for (const groupBase of knownGroups) {
      const matchingKeys: string[] = [];
      
      // Find translation keys that start with this group base
      for (const key of translationKeys) {
        if (key.startsWith(groupBase + '.') || key === groupBase) {
          matchingKeys.push(key);
        }
      }
      
      if (matchingKeys.length > 0) {
        groupPatterns.set(groupBase, matchingKeys);
      }
    }
    
    return groupPatterns;
  }
  
  private findGroupPatternUsage(line: string, basePattern: string): Array<{ column: number }> {
    const matches: Array<{ column: number }> = [];
    
    // Search patterns for the base group pattern
    const searchPatterns = [
      // Variable assignments
      new RegExp(`const\\s+\\w+\\s*=\\s*['"\`]${this.escapeRegex(basePattern)}['"\`]`, 'g'),
      new RegExp(`let\\s+\\w+\\s*=\\s*['"\`]${this.escapeRegex(basePattern)}['"\`]`, 'g'),
      new RegExp(`var\\s+\\w+\\s*=\\s*['"\`]${this.escapeRegex(basePattern)}['"\`]`, 'g'),
      
      // Object properties
      new RegExp(`['"\`]${this.escapeRegex(basePattern)}['"\`]\\s*:`, 'g'),
      
      // Template literal bases
      new RegExp(`\\\$\\{[^}]*['"\`]${this.escapeRegex(basePattern)}['"\`][^}]*\\}`, 'g'),
      
      // String concatenation
      new RegExp(`['"\`]${this.escapeRegex(basePattern)}['"\`]\\s*\\+`, 'g'),
      new RegExp(`\\+\\s*['"\`]${this.escapeRegex(basePattern)}['"\`]`, 'g'),
      
      // Function calls with base pattern
      new RegExp(`\\(\\s*['"\`]${this.escapeRegex(basePattern)}['"\`]`, 'g'),
      
      // Array elements
      new RegExp(`\\[\\s*['"\`]${this.escapeRegex(basePattern)}['"\`]`, 'g'),
      
      // General quoted occurrences (fallback)
      new RegExp(`['"\`]${this.escapeRegex(basePattern)}['"\`]`, 'g')
    ];
    
    for (const pattern of searchPatterns) {
      let match;
      while ((match = pattern.exec(line)) !== null) {
        matches.push({
          column: (match.index || 0) + 1 // 1-based indexing
        });
      }
      // Reset regex lastIndex to avoid issues with global flag
      pattern.lastIndex = 0;
    }
    
    // Remove duplicates based on column position
    const uniqueMatches = matches.filter((match, index, arr) => 
      arr.findIndex(m => m.column === match.column) === index
    );
    
    return uniqueMatches;
  }
  
  private escapeRegex(str: string): string {
    return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  }
}