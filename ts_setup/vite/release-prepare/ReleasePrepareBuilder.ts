import { ModuleRegistry, VersionsFile } from "../module-registry";
import { Command_LoadVersionsFile } from './Command_LoadVersionsFile'
import { Command_ResolveInitialVersions } from './Command_ResolveInitialVersions'
import { Command_HashBuildProfiles } from './Command_HashBuildProfiles'
import { Command_DetectChangesAndBump } from './Command_DetectChangesAndBump'
import { Command_UpdateAllPackageJson } from './Command_UpdateAllPackageJson'
import { Command_UpdateAllPackageJsonTsVersions } from './Command_UpdateAllPackageJsonTsVersions'
import { Command_SaveVersionsFile } from './Command_SaveVersionsFile'
import { Command_BackupPackageJson } from './Command_BackupPackageJson'
import { Command_ExtractGitHistory } from "./Command_ExtractGitHistory";
import { Command_SaveLogFile } from "./Command_SaveLogFile";


export interface VersioningBuilderOptions {
  versionsFilePath?: string;
  bumpType?: 'patch' | 'minor' | 'major';
  rootPath?: string;
}

export interface VersioningResult {
  changedProfiles: string[];
  unchangedProfiles: string[];
  updatedVersions: VersionsFile;
  updatedPackages: string[];
  versionsFilePath: string;
  summary: {
    totalProfiles: number;
    changedCount: number;
    unchangedCount: number;
    newProfiles: number;
  };
}

export class ReleasePrepareBuilder {
  private options: Required<VersioningBuilderOptions>;

  constructor(options: VersioningBuilderOptions = {}) {
    this.options = {
      versionsFilePath: './.modules/versions.json',
      bumpType: 'patch',
      rootPath: '.',
      ...options
    };
  }

  build(registry: ModuleRegistry): VersioningResult {
    console.log(`🚀 Release-Prepare Started`);


    // Get build profiles from registry
    const buildProfiles = Object.values(registry.buildProfiles);

    if (buildProfiles.length === 0) {
      throw new Error('❌  No build profiles found in registry');
    }
    console.log(`📋 Processing ${buildProfiles.length} build profiles`);

    // Command pipeline orchestration
    const loadVersionsCmd = new Command_LoadVersionsFile();
    const backupCmd = new Command_BackupPackageJson();

    const resolveInitialCmd = new Command_ResolveInitialVersions();
    const hashProfilesCmd = new Command_HashBuildProfiles();
    const detectChangesCmd = new Command_DetectChangesAndBump();
    const updatePackageJsonCmd = new Command_UpdateAllPackageJson();
    const saveVersionsCmd = new Command_SaveVersionsFile();
    const commitLogCmd = new Command_ExtractGitHistory();
    const versionTsCmd = new Command_UpdateAllPackageJsonTsVersions();
    const saveCommitLogCmd = new Command_SaveLogFile();


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
    const { updatedVersions, changedProfiles, unchangedProfiles } = detectChangesCmd.execute({
      versionsFile: initialVersions,
      profileHashes
    });

    // Step 5: Update package.json/version.ts files for changed profiles
    backupCmd.execute({ registry, buildProfiles, changedProfiles, rootPath: this.options.rootPath });
    const { updatedPackages, trace } = updatePackageJsonCmd.execute({
      registry,
      updatedVersions,
      changedProfiles,
      rootPath: this.options.rootPath
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

    // Step 7: Save commit log
    const { moduleLogs } = commitLogCmd.execute({ registry, rootPath: this.options.rootPath });
    const {} = saveCommitLogCmd.execute({
      versionsFile: updatedVersions,
      rootPath: this.options.rootPath,
      moduleLogs,
      registry,
      changedProfiles,
      trace,
    });


    // Calculate summary
    const newProfiles = buildProfiles.length - Object.keys(existingVersions).length;
    const summary = {
      totalProfiles: buildProfiles.length,
      changedCount: changedProfiles.length,
      unchangedCount: unchangedProfiles.length,
      newProfiles: Math.max(0, newProfiles)
    };

    console.log(`✅ Versioning completed:`);
    console.log(`   📊 Total profiles: ${summary.totalProfiles}`);
    console.log(`   🆕 New profiles: ${summary.newProfiles}`);
    console.log(`   🔄 Changed: ${summary.changedCount}`);
    console.log(`   ✅ Unchanged: ${summary.unchangedCount}`);
    console.log(`   📝 Updated packages: ${updatedPackages.length}`);

    return {
      changedProfiles,
      unchangedProfiles,
      updatedVersions,
      updatedPackages,
      versionsFilePath: savedPath,
      summary
    };

  }

}