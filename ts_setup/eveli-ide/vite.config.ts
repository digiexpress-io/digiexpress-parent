import { defineConfig, loadEnv } from 'vite';
import serveWrench from './vite.wrench.config';
import serveStencil from './vite.stencil.config';
import buildProd from './vite.prod.config';

// https://vitejs.dev/config/
export default defineConfig((props) => {
  const { command } = props;

  const serve = command === 'serve';
  const wrenchMode = serve && process.env.START_MODE === 'wrench';
  const stencilMode = serve && process.env.START_MODE === 'stencil';

  if (wrenchMode) {
    console.log('Wrench mode');
    return serveWrench(props);
  } else if (stencilMode) {
    console.log('Stencil mode');
    return serveStencil(props);
  } else if(command === 'serve') {
    throw new Error("unknown START_MODE: " + process.env.START_MODE + " in command: 'serve'!");
  }

  return buildProd(props);
});