import type { Plugin } from 'vite';
import { ModuleRegistryCacheBuilder } from '../module-registry-cache';
import { ModuleRegistryCommitLogBuilder } from '../module-registry';


export function buildChanges(user_options: { }): Plugin {
  return {
    name: 'build-changes',
    enforce: 'pre',

    config(config) {
      const rootPath = process.cwd();
      const registry = new ModuleRegistryCacheBuilder({
        onRegistryBuilt(registry) {
          const log = new ModuleRegistryCommitLogBuilder()
            .setRegistry(registry)
            .setCommitsCountAsFailsafe(500)
            .setCommitsWithMessageToIgnore(['chore: update deps', 'docs:', 'gamut release', 'eveli ide release', 'Preparing for maven release'])
            .setCommitsWithMessageAsPreviousRelease(['release: v', 'bump version'])
            .setBackendModule('backend', './mvn_setup')
            .setFrontendModule('./ts_setup')
            .setRootPath(rootPath)
            .build();

          console.log(log);
        },
      }).build(rootPath);
    },
    closeBundle(error) {

    }
  };
}
