
import { Main } from './core/Main';
import { Tabs } from './core/Tabs';
import { Secondary } from './core/Secondary';
import { Composer } from './core/context';

export * from './core/nav';
import WrenchClient, { HdesApi } from './core/client';
import Toolbar from './core/Toolbar';

export type { HdesApi };
export const WrenchComponents = { Toolbar, Main, Secondary, Tabs };
export { WrenchClient, Composer };