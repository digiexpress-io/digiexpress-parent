import React from 'react';

import Burger from 'components-burger';

import { Backend, Profile } from 'client';
import { ProjectsProvider } from 'descriptor-project';
import Views from './Views';



function appFrontoffice(backend: Backend, profile: Profile): Burger.App<{}, { backend: Backend, profile: Profile }> {
  return {
    id: "app-frontoffice",
    init: { backend, profile },
    components: {
      primary: Views.Main,
      secondary: Views.Secondary,
      toolbar: Views.Toolbar,
      context: ProjectsProvider
    },
    state: [
      (children: React.ReactNode, _restorePoint?: Burger.AppState<{}>) => (<>{children}</>),
      () => ({})
    ]
  }
}

export default appFrontoffice;
