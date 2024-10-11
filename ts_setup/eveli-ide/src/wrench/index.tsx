
import React from 'react';
import { Main } from './core/Main';
import { Secondary } from './core/Secondary';
import { Composer } from './core/context';


import WrenchClient, { HdesApi } from './core/client';
import Toolbar from './core/Toolbar';
import wrenchIntl from './core/intl';

import { SnackbarProvider } from 'notistack';
import * as Burger from '@/burger';
import { BurgerApi } from '@/burger';


interface WrenchComposerProps {
  service: HdesApi.Service,
  locked?: boolean;
};


const WrenchComposer: React.FC<WrenchComposerProps> = ({ service, locked }) => {
  if (locked === true) {
    return (<div>Content editing locked by deployment.</div>)
  }

  const composer: BurgerApi.App<Composer.ContextType> = {
    id: "wrench-composer",
    components: { primary: Main, secondary: Secondary, toolbar: Toolbar },
    state: [
      (children: React.ReactNode, restorePoint?: BurgerApi.AppState<Composer.ContextType>) => (<>{children}</>),
      () => ({})
    ]
  };
  return (
    /* @ts-ignore */
    <SnackbarProvider maxSnack={3}>
      <Composer.Provider service={service} >
        <Burger.Provider children={[composer]} secondary="toolbar.articles" drawerOpen />
      </Composer.Provider>
    </SnackbarProvider>
  );
}



export type { WrenchComposerProps, HdesApi };
export { WrenchComposer, WrenchClient, wrenchIntl };
