import React from 'react';

import * as API from './DrawerAPI';
import DrawerSessionData from './DrawerSessionData';
import { DrawerReducer, DrawerReducerDispatch } from './DrawerSessionReducer';

const DrawerContext = React.createContext<API.DrawerContextType>({
  session: {} as API.DrawerSession,
  actions: {} as API.DrawerActions,
})

const sessionInit: DrawerSessionData = new DrawerSessionData({})

const DrawerProvider: React.FC<{drawerOpen?: boolean}> = (props) => {
  const defaultDrawer = props.drawerOpen ? true : false;
  const [session, dispatch] = React.useReducer(DrawerReducer, sessionInit.withDrawer(defaultDrawer));
  const actions = React.useMemo(() => new DrawerReducerDispatch(dispatch), [dispatch]);
  
  React.useEffect(() => {
    actions.handleDrawerOpen(defaultDrawer);
  }, [defaultDrawer, actions])
  
  return (<DrawerContext.Provider value={{ session, actions }}>
      {props.children}
    </DrawerContext.Provider>);
}

const useDrawer = () => {
  const result: API.DrawerContextType = React.useContext(DrawerContext);
  return result;
}

export { DrawerProvider, useDrawer };