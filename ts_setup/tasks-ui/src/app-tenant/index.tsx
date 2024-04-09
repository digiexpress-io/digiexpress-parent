import React from 'react';

import Burger from 'components-burger';

import { Backend } from 'client';
import { TenantProvider } from 'descriptor-dialob';

import Views from './Views';
import { UserProfileAndOrg } from 'descriptor-access-mgmt';


function appTenant(backend: Backend, profile: UserProfileAndOrg): Burger.App<{}, { backend: Backend, profile: UserProfileAndOrg }> {
  return {
    id: "app-tenant",
    init: { backend, profile },
    components: {
      primary: Views.Main,
      secondary: Views.Secondary,
      toolbar: Views.Toolbar,
      context: TenantProvider
    },
    state: [
      (children: React.ReactNode, _restorePoint?: Burger.AppState<{}>) => (<>{children}</>),
      () => ({})
    ]
  }
}

export default appTenant;
