export declare namespace Command_ProcessDTSImports {
  export interface Input {
    filePath: string;
    content: string;
  }
  
  export interface Result {
    filePath: string;
    content: string;
  }
}

export class Command_ProcessDTSImports {
  execute(input: Command_ProcessDTSImports.Input): Command_ProcessDTSImports.Result {
    const { filePath, content } = input;
    
    if (!filePath.endsWith('.d.ts')) {
      // Skip non-declaration files
      return { filePath, content };
    }

    const processedContent = _cleanDTSImports(content);
    
    return {
      filePath,
      content: processedContent
    };
  }
}


// Pure transformative functions
function _cleanDTSImports(tsMess: string): string {
  const replacePairs = new Map<string, string>(); // suffix -> clean name
  const cleaned: string[] = [];

  // Step 1: Find all @dxs-ts import lines and collect replacements
  tsMess.split('\n').forEach((line) => {
    if (line.includes('@dxs-ts')) {
      const match = line.match(/import\s+\{\s*(\w+)\s+as\s+(\w+_\d+)\s*\}/);
      if (match) {
        const real = match[1]; // "BookingApi"
        const imaginary = match[2]; // "BookingApi_2"
        replacePairs.set(imaginary, real);
      }
      // Skip this line (don't add to cleaned)
    } else {
      cleaned.push(line);
    }
  });

  // Step 2: Apply replacements to clean up suffixes
  let content = cleaned.join('\n');
  replacePairs.forEach((real, imaginary) => {
    content = content.replaceAll(`${imaginary}.`, `${real}.`);
  });

  return content;
}