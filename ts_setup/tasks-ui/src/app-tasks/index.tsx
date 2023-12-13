import React from 'react';

import Burger from 'components-burger';
import { Backend, UserProfileAndOrg } from 'client';
import { TasksProvider } from 'descriptor-task';
import Views from './Views';


function appTasks(backend: Backend, profile: UserProfileAndOrg): Burger.App<{}, { backend: Backend, profile: UserProfileAndOrg }> {
  return {
    id: "app-tasks",
    init: { backend, profile },
    components: {
      primary: Views.Main,
      secondary: Views.Secondary,
      toolbar: Views.Toolbar,
      context: TasksProvider,
    },
    state: [
      (children: React.ReactNode, _restorePoint?: Burger.AppState<{}>) => (<>{children}</>),
      () => ({})
    ]
  }
}

export default appTasks;
