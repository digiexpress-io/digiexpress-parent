import { createRoot } from 'react-dom/client';
import { RouterProvider, createRouter } from '@tanstack/react-router'
import { GComponents, routeTree } from '@dxs-ts/gamut';

import { DemoApp } from './DemoApp';


// Create a new router instance
export const router = createRouter({ routeTree })

const container = document.getElementById('root');
const root = createRoot(container!);

root.render(<DemoApp><RouterProvider router={router} /></DemoApp>);


// Register the router instance for type safety
declare module '@tanstack/react-router' {
  interface Register {
    router: typeof router
  }
}

// Register gamut components
declare module "@mui/material" {
  export interface Components<Theme = unknown> extends GComponents<Theme> { }
}
