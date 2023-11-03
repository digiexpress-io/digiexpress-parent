import React from 'react';

import Burger from 'components-burger';

import Context from 'context';
import Views from './Views';


const appTasks: Burger.App<Context.ComposerContextType> = {
  id: "app-tasks",
  components: { primary: Views.Main, secondary: Views.Secondary, toolbar: Views.Toolbar },
  state: [
    (children: React.ReactNode, _restorePoint?: Burger.AppState<Context.ComposerContextType>) => (<>{children}</>),
    () => ({})
  ]
};

export default appTasks;
