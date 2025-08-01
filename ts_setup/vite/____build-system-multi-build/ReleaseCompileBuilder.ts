import { BuildProfile } from "../module-registry";
import { ReleasePrepareBuilder, VersioningResult } from '../release-prepare';
import { ReleasePublishBuilder } from '../release-publish';
import { ReleaseCompileBuilder } from '../release-compile'

export interface MultiBuildBuilderOptions {
  registryPath?: string;
  skipValidation?: boolean;
  dryRun?: boolean;
}

export interface MultiBuildResult {
  validProfiles: BuildProfile[];
  successfulBuilds: string[];
  publishedModules: string[];
  versioningResult: VersioningResult;
  commits: string[];
  summary: {
    totalProfiles: number;
    builtProfiles: number;
    publishedModules: number;
    commitsCreated: number;
  };
}

export class MultiBuildBuilder {
  private options: Required<MultiBuildBuilderOptions>;

  constructor(options: MultiBuildBuilderOptions = {}) {
    this.options = {
      registryPath: '.modules/registry.json',
      skipValidation: false,
      dryRun: false,
      ...options
    };
  }

  build(): MultiBuildResult {
    console.log(`🚀 Multi-Build Pipeline Started`);
    
    try {
      // Command orchestration
      const compileBuilder = new ReleaseCompileBuilder({ 
        registryPath: this.options.registryPath, 
        skipValidation: this.options.skipValidation 
      });
      const versioningBuilder = new ReleasePrepareBuilder();
      const publishBuilder = new ReleasePublishBuilder();



      // Step 1: Build all profiles
      const { successfulBuilds, registry, buildProfiles } = compileBuilder.build();

      // Step 4: Run versioning
      const versioningResult = versioningBuilder.build(registry);

      // Step 5: Publish changed modules
    
      const published = publishBuilder.build({ 
        dryRun: this.options.dryRun,
        buildProfiles,
        registry,
        successfulBuilds,
        versioning: versioningResult
      });

      return published;

    } catch (error) {
      console.error('💥 Multi-Build Pipeline Failed:', error);
      throw error;
    }
  }
}