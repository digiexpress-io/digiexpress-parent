import { defineConfig, loadEnv } from 'vite';
import serveWrench from './vite.wrench.config';
import serveStencil from './vite.stencil.config';
import serveFrontdesk from './vite.frontdesk.config';
import buildProd from './vite.prod.config';

// https://vitejs.dev/config/
export default defineConfig((props) => {
  const { command } = props;

  const serve = command === 'serve';
  const wrenchMode = serve && process.env.START_MODE === 'wrench';
  const stencilMode = serve && process.env.START_MODE === 'stencil';
  const frontdeskMode = serve && process.env.START_MODE === 'frontdesk';

  if (wrenchMode) {
    console.log('Wrench mode');
    return serveWrench(props);
  } else if (stencilMode || frontdeskMode) {
    console.log('Stencil mode');
    return serveStencil(props);
  } else if (frontdeskMode) {
    console.log('Frontdesk mode');
    return serveFrontdesk(props);
  }

  return buildProd(props);
});