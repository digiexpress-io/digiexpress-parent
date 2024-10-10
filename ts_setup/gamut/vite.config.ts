import { defineConfig, loadEnv } from 'vite';
import serveDev from './vite.dev.config';
import buildProd from './vite.prod.config';

// https://vitejs.dev/config/
export default defineConfig((props) => {
  const { command } = props;

  const serve = command === 'serve';
  const wrenchMode = serve && process.env.START_MODE === 'wrench';
  const stencilMode = serve && process.env.START_MODE === 'stencil';

  if (wrenchMode) {
    console.log('Wrench mode');
  } else if (stencilMode) {
    console.log('Stencil mode');
  } else if (serve) {
    return serveDev(props);
  } 

  return buildProd(props);
});