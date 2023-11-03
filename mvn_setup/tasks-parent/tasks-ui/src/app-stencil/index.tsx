import React from 'react';

import Burger from 'components-burger';
import { Main, Secondary, Toolbar, Composer, StencilClient } from 'components-stencil';



const appTasks: Burger.App<Composer.ContextType> = {
  id: "app-stencil",
  components: { primary: Main, secondary: Secondary, toolbar: Toolbar },
  state: [
    (children: React.ReactNode, _restorePoint?: Burger.AppState<Composer.ContextType>) => (<>{children}</>),
    () => ({})
  ]
};

export { Composer, StencilClient }
export default appTasks;
