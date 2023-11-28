import React from 'react';

import Burger from 'components-burger';

import { Backend, UserProfile } from 'client';
import { ProjectsProvider } from 'descriptor-project';
import { TasksProvider } from 'descriptor-task';
import { TenantProvider } from 'descriptor-tenant';

import Views from './Views';

const FrontOfficeProvider: React.FC<{ children: React.ReactNode, init: { backend: Backend, profile: UserProfile } }> = ({ children, init }) => {

  return (<ProjectsProvider init={init}>
    <TasksProvider init={init}>
      <TenantProvider init={init}>
        {children}
      </TenantProvider>
    </TasksProvider>
  </ProjectsProvider>);
}


function appFrontoffice(backend: Backend, profile: UserProfile): Burger.App<{}, { backend: Backend, profile: UserProfile }> {
  return {
    id: "app-frontoffice",
    init: { backend, profile },
    components: {
      primary: Views.Main,
      secondary: Views.Secondary,
      toolbar: Views.Toolbar,
      context: FrontOfficeProvider
    },
    state: [
      (children: React.ReactNode, _restorePoint?: Burger.AppState<{}>) => (<>{children}</>),
      () => ({})
    ]
  }
}

export default appFrontoffice;
