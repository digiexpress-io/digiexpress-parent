import { THEMES } from "./themes";

export declare namespace Command_RenderLogo {
  export interface Input {
    logo: string;
    theme: 'purple' | 'red';
  }
  export interface Result {
    success: boolean;
    renderedLines?: string[];
    error?: string;
  }
}


// Command Implementations
export class Command_RenderLogo {
  execute(input: Command_RenderLogo.Input): Command_RenderLogo.Result {
    try {
      const theme = THEMES[input.theme];
      const logoLines = input.logo.split('\n');
      const renderedLines: string[] = [];

      logoLines.forEach(line => {
        const spacedLine = line.replace(/ /g, '\u00A0');
        renderedLines.push(`%c${spacedLine}`);
      });

      return {
        success: true,
        renderedLines
      };
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : 'Unknown error rendering logo'
      };
    }
  }
}