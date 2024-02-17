import React from 'react';

import Burger from 'components-burger';

import { Backend } from 'client';


import { TasksProvider } from 'descriptor-task';
import { TenantProvider } from 'descriptor-dialob';
import { AvatarProvider } from 'descriptor-avatar';
import { UserProfileAndOrg } from 'descriptor-user-profile';

import Views from './Views';


const FrontOfficeProvider: React.FC<{ children: React.ReactNode, init: { backend: Backend, profile: UserProfileAndOrg } }> = ({ children, init }) => {
  return (
    <TasksProvider init={init}>
      <TenantProvider init={init}>
        <AvatarProvider>
          {children}
        </AvatarProvider>
      </TenantProvider>
    </TasksProvider>);
}


function appFrontoffice(backend: Backend, profile: UserProfileAndOrg): Burger.App<{}, { backend: Backend, profile: UserProfileAndOrg }> {
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
