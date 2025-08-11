
export type Line = 
  | { lineNumber: number; type: 'error', error: string, values: Record<string, string>, comments: string[] }
  | { lineNumber: number; type: 'header'; values: string[], comments: [] }
  | LineTranslation;

export type LineTranslation = { lineNumber: number; type: 'translations'; id: string; values: Record<string, string>, info: string, comments: string[] };

export interface ModuleUsage {
  moduleName: string;    // '@dxs-ts/auth-components'
  folders: string[];     // ['src/components', 'src/hooks']
  usageLocations?: {     // Optional: specific file locations
    file: string;
    line: number;
    column: number;
  }[];
}

export interface AnalyzedLine {
  source: Line;
  found: ModuleUsage[];
}

export interface ParseStats {
  totalLines: number;
  processedLines: number;
  skippedLines: number;
  emptyLines: number;
  commentLines: number;
  duplicateKeys: number;
  localesFound: Set<string>;
  keysProcessed: number;
}
