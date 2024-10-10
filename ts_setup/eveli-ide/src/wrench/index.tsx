
import React from 'react';
import { Main } from './core/Main';
import { Secondary } from './core/Secondary';
import WrenchClient from './core/client';
import Toolbar from './core/Toolbar';
import { Composer } from './core/context';
import wrenchIntl from './core/intl';

import { SnackbarProvider } from 'notistack';
import Burger from '@/burger';

interface WrenchComposerProps {
  service: WrenchClient.Service,
  locked?: boolean;
};


const WrenchComposer: React.FC<WrenchComposerProps> = ({ service, locked }) => {
  if (locked === true) {
    return (<div>Content editing locked by deployment.</div>)
  }

  const composer: Burger.App<Composer.ContextType> = {
    id: "wrench-composer",
    components: { primary: Main, secondary: Secondary, toolbar: Toolbar },
    state: [
      (children: React.ReactNode, restorePoint?: Burger.AppState<Composer.ContextType>) => (<>{children}</>),
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



export type { WrenchComposerProps };
export { WrenchComposer, WrenchClient, wrenchIntl };
