import { createRouter } from '@tanstack/react-router'

// Import the generated route tree
import { routeTree, FileRouteTypes, RootRouteChildren } from './routeTree.gen'

// Create a new router instance
export const router = createRouter({ routeTree })
export { routeTree }
export type { FileRouteTypes, RootRouteChildren }

// Register the router instance for type safety
declare module '@tanstack/react-router' {
  interface Register {
    router: typeof router
  }
}