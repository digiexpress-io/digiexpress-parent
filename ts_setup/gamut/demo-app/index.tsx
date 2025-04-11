import { createRoot } from 'react-dom/client';
import { RouterProvider } from '@tanstack/react-router'
import { GComponents, router, RouterType } from '@dxs-ts/gamut';

import { DemoApp } from './DemoApp';



const container = document.getElementById('root');
const root = createRoot(container!);

root.render(<DemoApp><RouterProvider router={router} /></DemoApp>);


// Register the router instance for type safety
declare module '@tanstack/react-router' {
  interface Register {
    router: RouterType
  }
}

// Register gamut components
declare module "@mui/material" {
  export interface Components<Theme = unknown> extends GComponents<Theme> { }
}
