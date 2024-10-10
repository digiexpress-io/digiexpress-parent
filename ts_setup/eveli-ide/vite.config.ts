import { defineConfig, loadEnv } from 'vite';
import serveWrench from './vite.wrench.config';
import serveStencil from './vite.stencil.config';
import buildProd from './vite.prod.config';

// https://vitejs.dev/config/
export default defineConfig((props) => {
  const { command } = props;
  const app: undefined | 'wrench' | 'stencil' = undefined;

  if(command === 'serve' && app === 'wrench') {
    return serveWrench(props);
  } else if(command === 'serve' && app === 'stencil') {
    return serveStencil(props);
  } else {
    throw new Error("unknown app: " + app + " in command: 'serve'!");
  }

  return buildProd(props);
});