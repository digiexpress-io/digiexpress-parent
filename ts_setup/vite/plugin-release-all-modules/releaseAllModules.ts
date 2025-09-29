import type { Plugin } from 'vite';

import { ReleaseCompileBuilder } from '../release-compile';
import { ReleasePrepareBuilder } from '../release-prepare';
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

      // Step 1: Build all profiles
      const does_it_compile_in_first_place = new ReleaseCompileBuilder({
        skipValidation: options.skipValidation, rootPath
      }).build();

      // Step 2: Run versioning
      const versioningResult = new ReleasePrepareBuilder().build(does_it_compile_in_first_place.registry);

      // Step 3: Recompile everything with trace and versioning info
      const { successfulBuilds, registry, buildProfiles } = new ReleaseCompileBuilder({
        skipValidation: options.skipValidation, rootPath
      }).build();

      // Step 4: Publish changed modules
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
