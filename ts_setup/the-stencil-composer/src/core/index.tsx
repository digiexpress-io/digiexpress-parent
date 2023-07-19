
import React from 'react';
import { Main } from './Main';
import { Secondary } from './Secondary';
import StencilClient from './client';
import Toolbar from './Toolbar';
import { Composer } from './context';
import messages from '../intl';

import { SnackbarProvider } from 'notistack';
import Burger from '@the-wrench-io/react-burger';

interface StencilComposerProps {
  service: StencilClient.Service,
  locked?: boolean;
};


const StencilComposer: React.FC<StencilComposerProps> = ({ service, locked }) => {

  if (locked === true) {
    return (<div>Content editing locked by deployment.</div>)
  }

  const composer: Burger.App<Composer.ContextType> = {
    id: "stencil-composer",
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

export type { StencilComposerProps };
export { StencilComposer, StencilClient, messages };
export { SiteCache, SessionData } from './context'
export * from './client/store';
export * from './Main';
export * from './Secondary';
export * from './ActivitiesView';
export * from './page';
export * from './link';
export * from './workflow';
export * from './article';
export * from './locale';
export * from './release';
export * from './migration';
export * from './template';
