import type { Plugin, UserConfig } from 'vite';

import { ModuleRegistryCacheBuilder } from '../module-registry-cache';
import { IntlAnalyzer } from '../plugin-intl-v2';


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
        .withInputCsv(props.inputCsv)
        .withOutputCsv(props.outputCsv)
        .withModuleRegistry(registry)
        .withTargetModules(props.moduleName)
        .withTargetModuleAlias({ '@dxs-ts/eveli': ['@dxs-ts/stencil', '@dxs-ts/wrench'] })
        .withKnownGroups(['feedback.main_topic', 'feedback.sub_topic', 'locale'])
        .build();

      return {
        
      }
    },
    closeBundle(error) {

    }
  };
}
