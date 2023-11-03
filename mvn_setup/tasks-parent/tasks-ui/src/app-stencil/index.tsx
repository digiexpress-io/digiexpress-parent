import React from 'react';

import Burger from 'components-burger';
import { Backend, Profile } from 'client';
import { Main, Secondary, Toolbar, Composer, StencilClient } from 'components-stencil';


function appStencil(backend: Backend, profile: Profile, projectId: string): Burger.App<{}, {
  service: StencilClient.Service
}> {

  const service = StencilClient.service({
    config: {
      url: backend.config.url + "stencil",
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
