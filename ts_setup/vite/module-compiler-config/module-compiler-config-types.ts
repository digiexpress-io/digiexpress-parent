import { LibraryFormats } from "vite";
import { BuildProfile, ModuleInfo, ModuleRegistry } from "../module-registry";

export interface ModuleCompilerOptions {
  moduleName: string;                    // Must match a package.json name in registry
  registryPath?: string;                 // Default: '.modules/registry.json'

  // Build options
  generateEntry?: boolean;
  bundleAnalysis?: boolean;

  customExternals?: string[];
  customGlobals?: Record<string, string>;

  // Validation options
  strictValidation?: boolean;
  failOnValidationErrors?: boolean;
  failOnCircularDeps?: boolean;
  failOnMissingModules?: boolean;
  failOnUnusedDeps?: boolean;
}


// Types for the output
export interface ModuleCompilerConfig {
  registry: ModuleRegistry;
  targetModuleInfo: ModuleInfo;
  buildProfile?: BuildProfile;
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
    };
    rollupOptions: {
      external: (id: string) => boolean;
      output: {
        globals: (id: string) => string;
      };
    };
  };

  metadata: () => {
    internalDependencies: string[]; 
    externalDependencies: Record<string, string>;
  };
}


