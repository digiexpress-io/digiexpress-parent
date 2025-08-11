import { Line } from "./intl-types";

export declare namespace Command_ExtractTranslations {
  export interface Input {
    lines: Line[];
    languages: string[];
  }
  export interface Result {
    translationLines: Line[];
    translationMap: Record<string, Record<string, string>>; // key -> lang -> value
  }
}

export class Command_ExtractTranslations {
  execute(input: Command_ExtractTranslations.Input): Command_ExtractTranslations.Result {
    console.log(`📝 Extracting translations for languages: ${input.languages.join(', ')}`);
    
    const translationLines = input.lines.filter(line => line.type === 'translations') as Extract<Line, { type: 'translations' }>[];
    const translationMap: Record<string, Record<string, string>> = {};
    
    for (const line of translationLines) {
      const key = line.id;
      if (!key) continue;
      
      translationMap[key] = {};
      for (const lang of input.languages) {
        if (line.values[lang]) {
          translationMap[key][lang] = line.values[lang];
        }
      }
    }
    
    console.log(`✅ Extracted ${Object.keys(translationMap).length} translation keys`);
    return { translationLines, translationMap };
  }
}