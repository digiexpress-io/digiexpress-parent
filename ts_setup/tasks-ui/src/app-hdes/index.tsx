import React from 'react';

import Burger from 'components-burger';
import { Backend, TenantConfig } from 'client';
import Client, { Main, Secondary, Toolbar, ComposerProvider } from 'components-hdes/core';
import { UserProfileAndOrg } from 'descriptor-user-profile';


function appHdes(backend: Backend, profile: UserProfileAndOrg, tenantConfig: TenantConfig): Burger.App<{}, {
  service: Client.Service,
}> {

  const projectId = tenantConfig.repoConfigs.find(c => c.repoType === 'HDES')?.repoId ?? ""
  const store: Client.Store = new Client.StoreImpl({ 
    url: backend.config.urls[0].url + "hdes",
  });
  const service = new Client.ServiceImpl(store);
  
  return {
    id: "app-hdes",
    init: {
      service
    },
    components: {
      primary: Main,
      secondary: Secondary,
      toolbar: Toolbar,
      context: ComposerProvider
    },
    state: [
      (children: React.ReactNode, _restorePoint?: Burger.AppState<{}>) => (<>{children}</>),
      () => ({})
    ]
  }
}

export default appHdes;
