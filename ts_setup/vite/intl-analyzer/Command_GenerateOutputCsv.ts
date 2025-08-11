import { AnalyzedLine, Line, LineTranslation } from "./intl-types";

export declare namespace Command_GenerateOutputCsv {
  export interface Input {
    moduleGroups: Record<string, AnalyzedLine[]>;
    orphanedGroup: Line[];
    languages: string[];
    outputPath: string;
  }
  export interface Result {
    outputPath: string;
    generatedAt: string;
    totalKeys: number;
  }
}



export class Command_GenerateOutputCsv {
  execute(input: Command_GenerateOutputCsv.Input): Command_GenerateOutputCsv.Result {
    console.log(`📄 Generating output CSV: ${input.outputPath}`);
    const lines: string[] = [];
    const header = ['id', 'info', ...input.languages].join(',');
    let totalKeys = 0;
    
    // Add module groups
    for (const [moduleName, group] of Object.entries(input.moduleGroups)) {
      lines.push(`// group - ${moduleName}`);
      
      for (const key of group) {
        const translations: LineTranslation = key.source as LineTranslation;
        lines.push(...translations.comments);
        const values = [translations.id, translations.info, ...input.languages.map(lang => translations.values[lang] || '')];
        lines.push(values.join(','));
        totalKeys++;
      }
      
      lines.push(''); // Empty line between groups
    }
    
    console.log(input.orphanedGroup.keys.length);

    //throw new Error("XYZ");

    // Add orphaned keys
    
    for (const line of input.orphanedGroup) {
      lines.push(`// ❌ ERROR: can\'t find matches in src code, ORIGIN: ${line.lineNumber}`);
      lines.push(...line.comments);
      const translations = line as LineTranslation;
      const values = [translations.id, translations.info, ...input.languages.map(lang => translations.values[lang] || '')];
      
      lines.push(values.join(','));
      totalKeys++;
    }
  
    
    const csvContent = [header, ...lines].join('\n');
    
    // TODO: Actually write to file
    console.log('Generated CSV:');
    console.log(csvContent);
    
    const generatedAt = new Date().toISOString();
    console.log(`✅ Generated ${totalKeys} keys to ${input.outputPath}`);
    
    return {
      outputPath: input.outputPath,
      generatedAt,
      totalKeys
    };
  }
}