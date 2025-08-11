import { ConfigEnv, UserConfig } from 'vite';
import { tanstackRouter } from '@tanstack/router-vite-plugin';
import { fetchVite, generateIntl, moduleRegistryCreate } from './vite'



// https://vitejs.dev/config/
export default function defineConfig(props: ConfigEnv): UserConfig {
  return {
    plugins: [
      generateIntl({ ignoreErrors: true, intlDirectory: './modules/gamut-intl', csv: './gamut/intl.csv' }),
      generateIntl({ ignoreErrors: true, intlDirectory: './modules/eveli-intl', csv: './eveli-ide/intl.csv' }),

      moduleRegistryCreate({ strict: true }),

      fetchVite({ 
        fetchDirectory: "./modules/eveli-api/fetch",
        fetchTreeDirectory: "./modules/eveli-api",
        fetchTreeGenFile: "./fetchTree.gen.ts",
        moduleName: "@dxs-ts/eveli-api"
      }),


      tanstackRouter({
        target: 'react',
        autoCodeSplitting: true,
        verboseFileRoutes: true,
        routesDirectory: './modules/gamut-routes/routes',
        generatedRouteTree: './modules/gamut-routes/routeTree.gen.ts'
      }),      

      tanstackRouter({
        target: 'react',
        autoCodeSplitting: true,
        verboseFileRoutes: true,
        routesDirectory: './modules/eveli-routes/routes',
        generatedRouteTree: './modules/eveli-routes/routeTree.gen.ts'
      }),      
    ],
    build: {
      lib: {
        entry: 'vite/dummy.js',
        name: 'dummy',
        fileName: (format) => `dummy.js`
      },
      rollupOptions: {
        external: ['es'],
      }
    }
  }
}