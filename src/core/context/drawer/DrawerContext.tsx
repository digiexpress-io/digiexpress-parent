import React from 'react';

import * as API from './DrawerAPI';
import DrawerSessionData from './DrawerSessionData';
import { DrawerReducer, DrawerReducerDispatch } from './DrawerSessionReducer';

const DrawerContext = React.createContext<API.DrawerContextType>({
  session: {} as API.DrawerSession,
  actions: {} as API.DrawerActions,
})

const sessionInit: DrawerSessionData = new DrawerSessionData({})

const DrawerProvider: React.FC<{drawerOpen?: boolean, children: React.ReactNode}> = (props) => {
  const [session, dispatch] = React.useReducer(DrawerReducer, sessionInit.withDrawer(props.drawerOpen ? true : false));
  const actions = React.useMemo(() => new DrawerReducerDispatch(dispatch), [dispatch]);
  
  return (<DrawerContext.Provider value={{ session, actions }}>
      {props.children}
    </DrawerContext.Provider>);
}

const useDrawer = () => {
  const result: API.DrawerContextType = React.useContext(DrawerContext);
  return result;
}

export { DrawerProvider, useDrawer };