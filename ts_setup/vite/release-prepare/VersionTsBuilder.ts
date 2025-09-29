import { ModuleRegistry } from "../module-registry";
import { Command_LoadVersionsFile } from './Command_LoadVersionsFile'
import { Command_ResolveInitialVersions } from './Command_ResolveInitialVersions'
import { Command_HashBuildProfiles } from './Command_HashBuildProfiles'
import { Command_DetectChangesAndBump } from './Command_DetectChangesAndBump'
import { Command_UpdateAllPackageJson } from './Command_UpdateAllPackageJson'
import { Command_UpdateAllPackageJsonTsVersions } from './Command_UpdateAllPackageJsonTsVersions'
import { Command_SaveVersionsFile } from './Command_SaveVersionsFile'


export interface VersionTsBuilderOptions {
  versionsFilePath?: string;
  bumpType?: 'patch' | 'minor' | 'major';
  rootPath?: string;
}

export interface VersionTsBuilderResult {
  versionsFilePath: string;
}

export class VersionTsBuilder {
  private options: Required<VersionTsBuilderOptions>;

  constructor(options: VersionTsBuilderOptions = {}) {
    this.options = {
      versionsFilePath: './.modules/versions.json',
      bumpType: 'patch',
      rootPath: '.',
      ...options
    };
  }

  build(registry: ModuleRegistry): VersionTsBuilderResult {
    // Get build profiles from registry
    const buildProfiles = Object.values(registry.buildProfiles);

    if (buildProfiles.length === 0) {
      throw new Error('❌  No build profiles found in registry');
    }


    // Command pipeline orchestration
    const loadVersionsCmd = new Command_LoadVersionsFile();
    const resolveInitialCmd = new Command_ResolveInitialVersions();
    const hashProfilesCmd = new Command_HashBuildProfiles();
    const detectChangesCmd = new Command_DetectChangesAndBump();


    const updatePackageJsonCmd = new Command_UpdateAllPackageJson();
    const saveVersionsCmd = new Command_SaveVersionsFile();
    const versionTsCmd = new Command_UpdateAllPackageJsonTsVersions();


    // Step 1: Load existing versions file
    const { versionsFile: existingVersions } = loadVersionsCmd.execute({
      versionsFilePath: this.options.versionsFilePath
    });

    // Step 2: Resolve initial versions (create entries for new profiles)
    const { initialVersions } = resolveInitialCmd.execute({
      buildProfiles,
      existingVersions
    });

    // Step 3: Hash all build profiles
    const { profileHashes } = hashProfilesCmd.execute({
      registry,
      buildProfiles,
      rootPath: this.options.rootPath
    });

    // Step 4: Detect changes and bump versions
    const { updatedVersions, changedProfiles } = detectChangesCmd.execute({
      versionsFile: initialVersions,
      profileHashes
    });

    // Step 5: Update package.json/version.ts files for changed profiles
    const { trace } = updatePackageJsonCmd.execute({
      registry,
      updatedVersions,
      changedProfiles,
      rootPath: this.options.rootPath,
      dryRun: true
    });
 
    versionTsCmd.execute({
      registry,
      updatedVersions,
      changedProfiles,
      trace,
      rootPath: this.options.rootPath
    });

    // Step 6: Save updated versions file
    const { savedPath } = saveVersionsCmd.execute({
      versionsFile: updatedVersions,
      versionsFilePath: this.options.versionsFilePath
    });

    console.log(`✅ version.ts updated, from json: ${savedPath}`);

    return {
      versionsFilePath: savedPath,
    };
  }

}