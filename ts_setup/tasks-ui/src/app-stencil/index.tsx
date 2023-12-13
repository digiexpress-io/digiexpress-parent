import React from 'react';

import Burger from 'components-burger';
import { Backend, UserProfileAndOrg, TenantConfig } from 'client';
import { Main, Secondary, Toolbar, Composer, StencilClient } from 'components-stencil';


function appStencil(backend: Backend, profile: UserProfileAndOrg, tenantConfig: TenantConfig): Burger.App<{}, {
  service: StencilClient.Service,
}> {

  const projectId = tenantConfig.repoConfigs.find(c => c.repoType === 'STENCIL')?.repoId ?? ""
  const service = StencilClient.service({
    config: {
      url: backend.config.urls[0].url + "stencil",
      projectId
    }
  });
  return {
    id: "app-stencil",
    init: {
      service
    },
    components: {
      primary: Main,
      secondary: Secondary,
      toolbar: Toolbar,
      context: Composer.Provider
    },
    state: [
      (children: React.ReactNode, _restorePoint?: Burger.AppState<{}>) => (<>{children}</>),
      () => ({})
    ]
  }
}

export default appStencil;
