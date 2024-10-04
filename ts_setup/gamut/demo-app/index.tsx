import { createRoot } from 'react-dom/client';
import { RouterProvider } from '@tanstack/react-router'
import { router } from '@dxs-ts/gamut';

import { DemoApp } from './DemoApp';



const container = document.getElementById('root');
const root = createRoot(container!);

root.render(<DemoApp><RouterProvider router={router} /></DemoApp>);
