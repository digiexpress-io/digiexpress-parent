import { Plugin } from 'vite'
import { ModuleRegistryCacheBuilder } from '../module-registry-cache';
import { ESLintConfigBuilder } from '../gen-eslint'
import { TSConfigBuilder } from '../gen-tsconfig'


export function moduleRegistryReload(): Plugin {
  return {
    name: 'module-registry-reload',

    configureServer(server) {
      // Ensure package.json files are watched
      server.watcher.add('**/package.json');
    },

    async handleHotUpdate(ctx) {
      const { file, server } = ctx;

      if (file.endsWith('package.json')) {
        console.log(`📦 Package.json changed: ${file}`);

        try {
          await _reloadRegistry(file);

          // Trigger full page reload instead of HMR
          server.ws.send({
            type: 'full-reload'
          });

        } catch (error) {
          console.error('❌ Package.json change handler failed:', error);

          // Send error to client
          server.ws.send({
            type: 'error',
            err: {
              message: `Package.json update failed: ${error.message}`,
              stack: error.stack
            }
          });
        }

        // Don't process this file for HMR
        return [];
      }
      // Let Vite handle other files
      return undefined;
    }
  };
}


async function _reloadRegistry(file: string, rootPath: string = process.cwd()) {
  new ModuleRegistryCacheBuilder({
    onRegistryBuilt(registry) {
      new TSConfigBuilder().build(registry);
      new ESLintConfigBuilder().build(registry);
    },
  }).build(rootPath, false);
}