
export interface ESLintConfigGeneratorOptions {
  outputDir?: string;              // Default: '.modules/eslint'
  includeExternalDeps?: boolean;   // Validate external dependencies too
  allowRelativeImports?: boolean;  // Allow relative imports within same module
  severity?: 'error' | 'warn';     // Rule severity level
}

export interface ESLintModuleConfig {
  moduleName: string;
  allowedInternalImports: string[];    // @dxs-ts/* modules this module can import
  blockedInternalImports: string[];    // @dxs-ts/* modules this module cannot import  
  allowedExternalImports: string[];    // External packages this module can import
  generatedRules: Record<string, any>; // The actual ESLint rules
}
