import { Line } from "./intl-types";

export declare namespace Command_DiscoverLanguages {
  export interface Input {
    lines: Line[];
  }
  export interface Result {
    detectedLanguages: string[];
    headerMapping: string[];
  }
}


export class Command_DiscoverLanguages {
  execute(input: Command_DiscoverLanguages.Input): Command_DiscoverLanguages.Result {
    console.log(`🌍 Discovering languages from ${input.lines.length} lines`);
    
    const headerLines = input.lines.filter(line => line.type === 'header') as Extract<Line, { type: 'header' }>[];
    if (headerLines.length === 0) {
      throw new Error('No header line found in CSV');
    }
    
    const headerMapping = headerLines[0].values;
    const detectedLanguages = headerMapping
      .filter(key => key.toLowerCase() !== 'id')
      .filter(key => key.length === 2); // ISO 639-1 language codes

    console.log(`✅ Detected languages: ${detectedLanguages.join(', ')}`);
    return { detectedLanguages, headerMapping };
  }
}
