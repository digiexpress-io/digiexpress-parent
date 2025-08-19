import type { Plugin, UserConfig } from 'vite';

import { ModuleRegistryCacheBuilder } from '../module-registry-cache';
import { IntlAnalyzer } from '../intl-analyzer';


export function buildOneModuleIntl(props: {
  moduleName: string;
  inputCsv: string;
  outputCsv: string;
}): Plugin {

  return {
    name: 'build-one-module',
    enforce: 'pre',

    config(config): UserConfig {
      const rootPath = process.cwd();
      const registry = new ModuleRegistryCacheBuilder().build(rootPath, false);
      const analyzer = new IntlAnalyzer()
        .withRootPath(rootPath)
        .withInputCsv(props.inputCsv)
        .withOutputCsv(props.outputCsv)
        .withModuleRegistry(registry)
        .withTargetModules(props.moduleName)
        .withTargetModuleAlias({ '@dxs-ts/eveli': ['@dxs-ts/stencil', '@dxs-ts/wrench'] })
        .withKnownGroups([
          'feedback.main_topic', 
          'feedback.sub_topic', 
          'locale', 
          'table', 
          'error', 
          'transferlist',
          'process.status',
          'publications',
          'task.status',
          'task.priority',
          'calendar',
          'eveli.userProfile.tenantConfig.select'])
        .build();

      return {
        
      }
    },
    closeBundle(error) {

    }
  };
}
