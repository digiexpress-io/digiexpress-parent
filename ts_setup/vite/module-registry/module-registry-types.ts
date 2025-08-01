// ============================================================================
// MODULE ECOSYSTEM TYPES
// ============================================================================
// Core data structures for representing a TypeScript monorepo with fake 
// modules and dependency relationships.
// ============================================================================

import { LibraryFormats } from "vite";

export interface ModuleInfo {
  name: string;                    // '@dxs-ts/gamut-api'
  path: string;                    // 'src/modules/gamut-api'
  packageJson: Record<string, any>;
  dependencies: string[];          // All dependencies from package.json
  peerDependencies: string[];      // Peer dependencies from package.json
  externalDependencies: string[];  // Non-@dxs-ts dependencies (computed)
  internalDependencies: string[];  // @dxs-ts dependencies (computed)
  
  // NEW: Actual usage data from source code analysis
  actualDependencies: string[];    // Dependencies actually used in source files
  actualExternalDependencies: string[];  // External deps actually used
  actualInternalDependencies: string[];  // Internal deps actually used
  missingDependencies: string[];   // Used but not declared
  unusedDependencies: string[];    // Declared but not used
}

// ============================================================================
// DEPENDENCY GRAPH
// ============================================================================

export interface DependencyGraph {
  forward: Record<string, string[]>;   // module -> modules it depends on
  reverse: Record<string, string[]>;   // module -> modules that depend on it
  circularDependencies: string[][];    // detected circular dependency chains
}

// ============================================================================
// BUILD PROFILES
// ============================================================================

export interface BuildProfile {
  name: string;                    // 'lib-gamut'
  path: string;                    // 'modules/lib-gamut'
  entryPoint: string;              // path to entry file
  entryModule: string;             // '@dxs-ts/gamut' - the main module
  includedModules: string[];       // modules to bundle together
  externalDependencies: string[];  // dependencies to externalize
}

// ============================================================================
// MODULE REGISTRY
// ============================================================================

export interface ModuleRegistry {
  modules: Record<string, ModuleInfo>;
  dependencyGraph: DependencyGraph;
  buildProfiles: Record<string, BuildProfile>;
  rootPath: string;
  checksum: string;                    // Hash of all module files and package.json contents
  generatedAt: string;                 // ISO timestamp when registry was built
}

// ============================================================================
// VALIDATION TYPES
// ============================================================================

export type ValidationErrorType = 
  | 'missing_external'
  | 'unused_external' 
  | 'missing_internal'
  | 'unused_internal'
  | 'circular_dependency'
  | 'missing_module'
  | 'invalid_structure'
  | 'corruption_error'
  | 'dependency_tree_corruption'
  | 'circular_dependency_corruption';

export interface ValidationError {
  type: ValidationErrorType;
  severity: 'error' | 'warning' | 'corruption';
  moduleName: string;
  item: string;
  problem: string;
  solution: string;
  technicalDetails?: string;
  location?: {
    file?: string;
    line?: number;
    column?: number;
  };
}

export interface ValidationResult {
  targetModule?: string;
  validatedModules: string[];
  errors: ValidationError[];
  warnings: ValidationError[];
  corruptions: ValidationError[];
  isValid: boolean;
  isCorrupted: boolean;
  summary: string;
  validationTime: number;
}

export interface ValidationOptions {
  failOnValidationErrors?: boolean;        // Default: true - Fail build on validation errors
  failOnCircularDeps?: boolean;           // Default: true - Fail build on circular dependencies  
  failOnMissingModules?: boolean;         // Default: true - Fail build on missing modules
  failOnUnusedDeps?: boolean;             // Default: false - Fail build on unused dependencies
  throwOnErrors?: boolean;                // Default: true - Should validation throw or just report?
  strictPerModuleValidation?: boolean;    // Default: false - NEW: Per-module vs aggregate validation
}



export interface VersionEntry {
  moduleName: string;
  version: string;
  srcHash: string;
  lastPublished: string;
}

export interface VersionsFile {
  [buildProfileName: string]: VersionEntry;
}

// Types for the output
export interface BuildConfig {
  targetModuleInfo: ModuleInfo;
  buildProfile: BuildProfile;
  resolve: {
    alias: Record<string, string>;
  };
  build: {
    outDir: string;
    lib: {
      entry: string;
      name: string;
      fileName: string;
      formats: LibraryFormats[];
    },
  },
  metadata: {
    internalDependencies: string[]; 
    externalDependencies: Record<string, string>;
  }
}