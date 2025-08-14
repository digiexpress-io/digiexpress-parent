import { defineConfig } from 'vite';


import buildModules from './vite.config.build.modules';
import startGamut from './vite.config.start.gamut';
import startEveli from './vite.config.start.eveli';
import uniBuild from './vite.config.build.uni-build';
import publishModules from './vite.config.build.publish';
import buildIntl from './vite.config.build.intl';
import publishDryRun from './vite.config.build.publish.dry.run';

// https://vitejs.dev/config/
export default defineConfig((props) => {
  if (process.env.START_MODE === 'uni-build') {
    return uniBuild(props);
  }
  if (process.env.START_MODE === 'publish') {
    return publishModules(props);
  }
  if (process.env.START_MODE === 'build-modules') {
    return buildModules(props);
  }
  if (process.env.START_MODE === 'start-gamut') {
    return startGamut(props);
  }
  if (process.env.START_MODE === 'start-eveli') {
    return startEveli(props);
  }
  if (process.env.START_MODE === 'intl-build') {
    return buildIntl(props);
  }
  if(process.env.START_MODE === 'publish-dry-run') {
    return publishDryRun(props);
  }
  throw new Error('woops');
});
