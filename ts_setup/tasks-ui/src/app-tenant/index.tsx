import React from 'react';

import Burger from 'components-burger';

import { Backend, UserProfile } from 'client';
import { TenantProvider } from 'descriptor-tenant';

import Views from './Views';


function appTenant(backend: Backend, profile: UserProfile): Burger.App<{}, { backend: Backend, profile: UserProfile }> {
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
