import { defineConfig } from 'vite';
import serveDev from './vite.dev.config';
import buildProd from './vite.prod.config';

// https://vitejs.dev/config/
export default defineConfig((props) => {
  const { command } = props;

  if (command === 'serve') {
    return serveDev(props);
  }

  return buildProd(props);
});