import { BuildProfile, ModuleRegistry } from "../module-registry";
import { Command_ValidateBuildOutputs } from "./Command_ValidateBuildOutputs";
import { Command_ValidateRegistry } from "./Command_ValidateRegistry";
import { Command_BuildAllProfiles } from "./Command_BuildAllProfiles";


export interface ReleaseCompileOptions {
  registryPath?: string;
  skipValidation?: boolean;
}

export interface ReleaseCompileResult {
  registry: ModuleRegistry;
  successfulBuilds: string[];
  buildProfiles: BuildProfile[];
}

export class ReleaseCompileBuilder {
  private options: Required<ReleaseCompileOptions>;

  constructor(options: ReleaseCompileOptions = {}) {
    this.options = {
      registryPath: '.modules/registry.json',
      skipValidation: false,
      ...options
    };
  }

  build(): ReleaseCompileResult {
    console.log(`🚀 Release-Compile Started`);

    // Command orchestration
    const validateRegistryCmd = new Command_ValidateRegistry();
    const validateOutputsCmd = new Command_ValidateBuildOutputs();
    const buildAllCmd = new Command_BuildAllProfiles();


    // Step 1: Validate registry
    const { registry } = validateRegistryCmd.execute({
      registryPath: this.options.registryPath
    });

    const buildProfiles = Object.values(registry.buildProfiles);

    // Step 2: Validate build outputs
    if (!this.options.skipValidation) {
      const { validProfiles, invalidProfiles } = validateOutputsCmd.execute({
        registry,
        buildProfiles
      });

      if (invalidProfiles.length > 0) {
        throw new Error(`${invalidProfiles.length} profiles have invalid build outputs`);
      }
    }

    // Step 3: Build all profiles
    const { successfulBuilds } = buildAllCmd.execute({
      buildProfiles,
      registry
    });

    return { registry, buildProfiles, successfulBuilds }

  }
}