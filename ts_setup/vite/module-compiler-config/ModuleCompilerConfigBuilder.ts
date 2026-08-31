import { Command_LoadRegistry } from "./Command_LoadRegistry";
import { Command_ValidateLibBuild } from "./Command_ValidateLibBuild";
import { Command_ResolveTargetModule } from "./Command_ResolveTargetModule";
import { Command_GenerateLibConfig } from "./Command_GenerateLibConfig";
import { Command_ResolvePackageVersions } from "./Command_ResolvePackageVersions";
import { ModuleCompilerOptions, ModuleCompilerConfig } from "./module-compiler-config-types";


export class ModuleCompilerConfigBuilder {
  private options: Required<ModuleCompilerOptions>;

  constructor(options: ModuleCompilerOptions) {
    this.options = {
      registryPath: '.modules/registry.json',
      generateEntry: false,
      bundleAnalysis: true,
      customExternals: [],
      customGlobals: {},
      strictValidation: true,
      failOnValidationErrors: true,
      failOnCircularDeps: true,
      failOnMissingModules: true,
      failOnUnusedDeps: false,
      ...options
    };
  }

  build(registryRecreate: boolean, rootPath: string = process.cwd()): ModuleCompilerConfig {
    const { moduleName, registryPath } = this.options;
    try {
      // Command pipeline orchestration
      const loadRegistryCmd = new Command_LoadRegistry();
      const validateLibBuildCmd = new Command_ValidateLibBuild();
      const resolveTargetModuleCmd = new Command_ResolveTargetModule();
      const generateLibConfigCmd = new Command_GenerateLibConfig();
      const resolvePackageVersionsCmd = new Command_ResolvePackageVersions();

      // Step 1: Load registry file
      const { registry } = loadRegistryCmd.execute({
        rootPath,
        registryPath,
        registryRecreate
      });

      // Step 2: Validate registry
      validateLibBuildCmd.execute({
        registry,
        moduleName,
        options: {
          strictValidation: this.options.strictValidation,
          failOnValidationErrors: this.options.failOnValidationErrors,
          failOnCircularDeps: this.options.failOnCircularDeps,
          failOnMissingModules: this.options.failOnMissingModules,
          failOnUnusedDeps: this.options.failOnUnusedDeps
        }
      });

      // Step 3: Resolve target module
      const { targetModuleInfo } = resolveTargetModuleCmd.execute({
        registry,
        moduleName
      });

      // Step 4: Generate lib configuration
      const configResult = generateLibConfigCmd.execute({
        registry,
        targetModuleInfo,
        rootPath,
        options: {
          customExternals: this.options.customExternals,
          customGlobals: this.options.customGlobals,
          generateEntry: this.options.generateEntry
        }
      });

      // Step 5: Resolve dependencies

      const rollupResolved: RollupResolved = { external: [], global: [] }

      // Assemble final build configuration
      const libBuildConfig: ModuleCompilerConfig = {
        registry,
        targetModuleInfo,
        buildProfile: configResult.buildProfile,
        resolve: {
          alias: configResult.internalAliases
        },
        build: {
          outDir: configResult.libConfig.outputDir,
          lib: {
            entry: configResult.libConfig.entry,
            name: configResult.libConfig.name,
            fileName: configResult.libConfig.fileName,
            formats: ['es']
          },
          rollupOptions: {
            external: (id: string) => _isExternal(id, configResult.allExternals, rollupResolved),
            output: {
              globals: (id: string) => _resolveGlobal(id, configResult.globals, rollupResolved)
            }
          }
        },
        // Metadata for build analysis
        metadata: () => {

          const resolveDeps = resolvePackageVersionsCmd.execute(rollupResolved);
          if(resolveDeps.unresolvedPackages.length > 0) {
            throw new Error('Failed to resolve depdendencies!');
          }
          return {
            internalDependencies: configResult.internalDependencies,
            externalDependencies: resolveDeps.resolvedVersions,
          }          
        }
      };

      return libBuildConfig;

    } catch (error) {
      //console.error('❌ Failed to generate lib build config:', error);
      throw error;
    }
  }
}

interface RollupResolved {
  external: string[], 
  global: string[];
}

// Pure utility functions for the build config
function _isExternal(id: string, externals: string[], rollupResolved: RollupResolved): boolean {
  
  if(!rollupResolved.external.includes(id)) {
    rollupResolved.external.push(id);
  }

  // Handle MUI subpaths
  if (id.startsWith('@mui')) {
    return true;
  }
  // mangled elk
  if(id.endsWith('elk.bundled.js')) {
    return true;
  }
  // Handle other external packages
  return externals.includes(id);
}

function _resolveGlobal(id: string, globalMappings: Record<string, string>, rollupResolved: RollupResolved): string {
  if(!rollupResolved.global.includes(id)) {
    rollupResolved.global.push(id);
  }

  // Handle MUI icons subpaths
  if (id.startsWith('@mui/icons-material/')) {
    const iconName = id.replace('@mui/icons-material/', '');
    return `MuiIconsMaterial.${iconName}`;
  }

  // Handle other globals
  return globalMappings[id] || id;
}

