import { defineConfig } from 'vite';
import serveFrontdesk from './vite.frontdesk.config';

import buildProd from './vite.prod.config';

// https://vitejs.dev/config/
export default defineConfig((props) => {
  const { command } = props;
  const serve = command === 'serve';

  const frontdeskMode = serve && process.env.START_MODE === 'frontdesk';

  if (frontdeskMode) {
    console.log('Frontdesk mode');
    return serveFrontdesk(props);
  }

  return buildProd(props);
});