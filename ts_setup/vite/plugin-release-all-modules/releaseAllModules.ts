import type { Plugin } from 'vite';

import { ReleaseCompileBuilder } from '../release-compile';
import { ReleasePrepareBuilder, VersionTsBuilder } from '../release-prepare';
import { ReleasePublishBuilder, ReleasePublishResult } from '../release-publish';


export function releaseAllModules(options: {
  skipValidation: false,
  dryRun: boolean,
  branchName: string
}): Plugin {

  let published: ReleasePublishResult;

  return {
    name: 'release-all-modules',
    buildStart() {
      const rootPath: string = process.cwd();


      // do the semi dry run, where we generate version info and what is actually used in the compiler
      {
       const { registry }  = new ReleaseCompileBuilder({
          skipValidation: options.skipValidation, rootPath
        }).build();
        new VersionTsBuilder().build(JSON.parse(JSON.stringify(registry)));
      }


      // Step 1: Build all profiles
      const { successfulBuilds, registry, buildProfiles }  = new ReleaseCompileBuilder({
        skipValidation: options.skipValidation, rootPath
      }).build();

      // Step 2: Run versioning
      const versioningResult = new ReleasePrepareBuilder().build(registry);

      // Step 3: Publish changed modules
      published = new ReleasePublishBuilder().build({
        dryRun: options.dryRun,
        buildProfiles,
        registry,
        successfulBuilds,
        versioning: versioningResult,
        rootPath,
        branchName: options.branchName
      });
    },

    buildEnd(error) {
      if (error) {

      } else {

      }
    },
  };
}
