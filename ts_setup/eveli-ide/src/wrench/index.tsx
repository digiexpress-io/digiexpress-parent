
import { Main } from './core/Main';
import { Secondary } from './core/Secondary';
import { Composer } from './core/context';


import WrenchClient, { HdesApi } from './core/client';
import Toolbar from './core/Toolbar';

export type { HdesApi };
export const WrenchComponents = { Toolbar, Main, Secondary };
export { WrenchClient, Composer };