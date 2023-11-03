import React from 'react';

import Burger from 'components-burger';

import Context from 'context';
import Views from './Views';


const appProjects: Burger.App<Context.ComposerContextType> = {
  id: "app-projects",
  components: { primary: Views.Main, secondary: Views.Secondary, toolbar: Views.Toolbar },
  state: [
    (children: React.ReactNode, _restorePoint?: Burger.AppState<Context.ComposerContextType>) => (<>{children}</>),
    () => ({})
  ]
};

export default appProjects;
