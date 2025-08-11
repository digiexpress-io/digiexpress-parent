import type { Plugin } from 'vite';

import { ReleaseCompileBuilder } from '../release-compile';
import { ReleasePrepareBuilder } from '../release-prepare';
import { ReleasePublishBuilder, ReleasePublishResult } from '../release-publish';


export function releaseAllModules(options: {
  skipValidation: false,
  dryRun: true
}): Plugin {

  let published: ReleasePublishResult;

  return {
    name: 'release-all-modules',
    buildStart() {
      
      // Step 1: Build all profiles
      const { successfulBuilds, registry, buildProfiles } = new ReleaseCompileBuilder({
        skipValidation: options.skipValidation
      }).build();

      // Step 2: Run versioning
      const versioningResult = new ReleasePrepareBuilder().build(registry);

      // Step 3: Publish changed modules
      published = new ReleasePublishBuilder().build({
        dryRun: options.dryRun,
        buildProfiles,
        registry,
        successfulBuilds,
        versioning: versioningResult
      });
    },

    buildEnd(error) {
      if (error) {

      } else {

      }
    },
  };
}
