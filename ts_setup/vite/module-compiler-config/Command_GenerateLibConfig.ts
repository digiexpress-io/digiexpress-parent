import { resolve } from 'node:path';
import { ModuleRegistry, ModuleInfo, BuildProfile } from '../module-registry'

export declare namespace Command_GenerateLibConfig {
  export interface Input {
    registry: ModuleRegistry;
    targetModuleInfo: ModuleInfo;
    rootPath: string;
    options: {
      customExternals: string[];
      customGlobals: Record<string, string>;
      generateEntry: boolean;
    };
  }

  export interface Result {
    buildProfile?: BuildProfile;
    internalDependencies: string[];
    externalDependencies: string[];
    internalAliases: Record<string, string>;
    globals: Record<string, string>;
    allExternals: string[];
    libConfig: {
      entry: string;
      name: string;
      fileName: string;
      outputDir: string;
    };
  }
}

export class Command_GenerateLibConfig {
  execute(input: Command_GenerateLibConfig.Input): Command_GenerateLibConfig.Result {
    const { registry, targetModuleInfo, rootPath, options } = input;

    // Find build profile (optional)
    let buildProfile: BuildProfile | undefined;
    for (const [profileName, profile] of Object.entries(registry.buildProfiles)) {
      if (profile.entryModule === targetModuleInfo.name) {
        buildProfile = profile;
        console.log(`📋 Found build profile: ${profileName}`);
        break;
      }
    }

    if (!buildProfile) {
      console.log(`📋 No build profile found, using auto-generated config`);
    }

    // Build configuration using resolved data
    const internalDependencies = targetModuleInfo.internalDependencies || [];
    const externalDependencies = targetModuleInfo.externalDependencies || [];

    // Create aliases for internal dependencies
    const internalAliases = _createInternalAliases(registry, internalDependencies, rootPath);

    // Externalize dependencies
    const allExternals = _collectAllExternals(
      externalDependencies,
      options.customExternals,
      buildProfile?.externalDependencies ?? []
    );

    // Generate globals
    const globals = _generateGlobals(allExternals, options.customGlobals);

    // Generate lib-specific config
    const libConfig = _generateLibConfig(targetModuleInfo, buildProfile);

    // console.log(`📊 Build config: ${internalDependencies.length} internal, ${externalDependencies.length} external deps`);

    return {
      buildProfile,
      internalDependencies,
      externalDependencies,
      internalAliases,
      globals,
      allExternals,
      libConfig
    };
  }
}

// Pure transformative functions
function _createInternalAliases(registry: ModuleRegistry, internalDependencies: string[], rootPath: string): Record<string, string> {
  const internalAliases: Record<string, string> = {};

  for (const dep of internalDependencies) {
    const depModule = registry.modules[dep];
    if (depModule) {
      internalAliases[dep] = resolve(rootPath, depModule.path, 'index.ts');
    }
  }

  return internalAliases;
}

function _collectAllExternals(externalDependencies: string[], customExternals: string[], profileExternals: string[]): string[] {
  return [
    ...externalDependencies,
    ...customExternals,
    ...profileExternals
  ].filter((value, index, array) => array.indexOf(value) === index); // Remove duplicates
}

function _generateGlobals(externals: string[], customGlobals: Record<string, string>): Record<string, string> {
  const defaultMappings: Record<string, string> = {};
  const globals: Record<string, string> = {};

  for (const external of externals) {
    if (defaultMappings[external]) {
      globals[external] = defaultMappings[external];
    } else {
      globals[external] = external
        .replace(/[@\/\-]/g, '')
        .replace(/^\w/, c => c.toUpperCase());
    }
  }

  return { ...globals, ...customGlobals };
}

function _generateLibConfig(targetModuleInfo: ModuleInfo, buildProfile?: BuildProfile): {
  entry: string;
  name: string;
  fileName: string;
  outputDir: string;
} {
  const fileName = 'index';
  const outputDir = `${targetModuleInfo.path}/dist`;
  const entry = buildProfile?.entryPoint || `./${targetModuleInfo.path}/index.ts`;
  const name = targetModuleInfo.name;

  return {
    entry,
    name,
    fileName,
    outputDir
  };
}