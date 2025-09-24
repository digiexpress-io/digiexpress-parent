import { createRouter } from '@tanstack/react-router'
import { GShellClassName } from '@dxs-ts/gamut-shell'
import { routeTree } from './routeTree.gen'



export { routeTree }
export type { FileRouteTypes, RootRouteChildren } from './routeTree.gen'
export const router = createRouter({
  routeTree,
  scrollRestorationBehavior: 'instant',
  scrollRestoration: true,
  scrollToTopSelectors: [`.${GShellClassName}`]
})

// Register the router instance for type safety
declare module '@tanstack/react-router' {
  interface Register {
    router: typeof router
  }
}