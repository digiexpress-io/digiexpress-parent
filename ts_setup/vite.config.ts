import { defineConfig } from 'vite';


import buildModules from './vite.config.build.modules';
import startGamut from './vite.config.start.gamut';
import uniBuild from './vite.config.build.uni-build';
import publishModules from './vite.config.build.publish';


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

  throw new Error('woops');
});
