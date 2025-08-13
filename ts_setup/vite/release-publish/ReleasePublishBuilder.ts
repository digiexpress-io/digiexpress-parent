import { BuildProfile, ModuleRegistry } from "../module-registry";
import { VersioningResult } from '../release-prepare';

import { Command_RestorePackageJsons } from "./Command_RestorePackageJsons";

import { Command_PublishChangedModules } from "./Command_PublishChangedModules";
import { Command_GitCommit } from "./Command_GitCommit";


export interface ReleasePublishOptions {
  buildProfiles: BuildProfile[];
  successfulBuilds: string[];
  registry: ModuleRegistry;
  versioning: VersioningResult;
  dryRun: boolean;
  rootPath: string;
}

export interface ReleasePublishResult {
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

export class ReleasePublishBuilder {


  build(options: ReleasePublishOptions): ReleasePublishResult {
    console.log(`🚀 Release-Publish Started`);
    const { buildProfiles, versioning: versioningResult, registry, successfulBuilds } = options;

    
    if (options.dryRun) {
      console.log(`🔍 Dry run - would publish: ${versioningResult.changedProfiles.join(', ')}`);
      return this.createResult(buildProfiles, successfulBuilds, [], versioningResult, []);
    }

    const gitCommitCmd = new Command_GitCommit();
    const restoreCmd = new Command_RestorePackageJsons();
    const publishCmd = new Command_PublishChangedModules();

    // Step 5: Publish changed modules
    const { publishedModules } = publishCmd.execute({
      registry,
      changedProfiles: versioningResult.changedProfiles,
      updatedVersions: versioningResult.updatedVersions,
      rootPath: options.rootPath
    });

    // Step 6: Git commit with version changes
    const commits: string[] = [];
    if (publishedModules.length > 0) {
      const versionCommit = gitCommitCmd.execute({
        message: `chore: release versions - ${publishedModules.join(', ')}`
      });
      if (versionCommit.success && versionCommit.commitHash) {
        commits.push(versionCommit.commitHash);
      }
    }

    // Step 7: Restore package.json files
    restoreCmd.execute({
      registry,
      changedProfiles: versioningResult.changedProfiles,
      updatedVersions: versioningResult.updatedVersions,
      rootPath: options.rootPath
    });

    // Step 8: Git commit for next iteration
    const nextIterationCommit = gitCommitCmd.execute({
      message: 'chore: preparing for next iteration'
    });
    if (nextIterationCommit.success && nextIterationCommit.commitHash) {
      commits.push(nextIterationCommit.commitHash);
    }

    const result = this.createResult(buildProfiles, successfulBuilds, publishedModules, versioningResult, commits);

    console.log(`🎉 Multi-Build Pipeline Completed Successfully!`);
    console.log(`   📊 Built: ${result.summary.builtProfiles} profiles`);
    console.log(`   📦 Published: ${result.summary.publishedModules} modules`);
    console.log(`   📝 Commits: ${result.summary.commitsCreated}`);

    return result;


  }

  private createResult(
    buildProfiles: BuildProfile[],
    successfulBuilds: string[],
    publishedModules: string[],
    versioningResult: VersioningResult,
    commits: string[]
  ): ReleasePublishResult {
    return {
      validProfiles: buildProfiles,
      successfulBuilds,
      publishedModules,
      versioningResult,
      commits,
      summary: {
        totalProfiles: buildProfiles.length,
        builtProfiles: successfulBuilds.length,
        publishedModules: publishedModules.length,
        commitsCreated: commits.length
      }
    };
  }
}