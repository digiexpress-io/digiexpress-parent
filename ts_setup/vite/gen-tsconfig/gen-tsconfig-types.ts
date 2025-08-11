export interface TSConfigGeneratorOptions {
  outputPath?: string;                    // Default: 'tsconfig.gen.json'
  baseUrl?: string;                       // Default: '.'
  includeExternalDeps?: boolean;          // Include external dependencies in paths
  compilerOptions?: Record<string, any>;  // Additional compiler options
}

export interface TSConfigOutput {
  compilerOptions: {
    baseUrl: string;
    paths: Record<string, string[]>;
    [key: string]: any;
  };
  extends?: string; 
  references?: Array<{ path: string }>;
  // Generated metadata (not part of actual tsconfig)
  _generated: {
    by: string;
    at: string;
    fromRegistry: string;
    forModule?: string;
    moduleCount?: number;
    alias?: Record<string, string[]>
  };
}
