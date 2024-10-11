import { defineConfig } from 'vite';
import buildProd from './vite.prod.config';

// https://vitejs.dev/config/
export default defineConfig((props) => {
  const { command } = props;

  return buildProd(props);
});