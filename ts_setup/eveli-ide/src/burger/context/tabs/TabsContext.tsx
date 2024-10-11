import React from 'react';

import { BurgerApi } from '../../BurgerApi';
import TabsSessionData from './TabsSessionData';
import { TabsReducer, TabsReducerDispatch } from './TabsReducer';


const TabsContext = React.createContext<BurgerApi.TabsContextType>({
  session: {} as BurgerApi.TabsSession,
  actions: {} as BurgerApi.TabsActions,
})

const sessionInit: TabsSessionData = new TabsSessionData({appId: ""})


const TabsProvider: React.FC<{appId: string, children: React.ReactNode}> = ({appId, children}) => {
  const [session, dispatch] = React.useReducer(TabsReducer, sessionInit.withAppId(appId));
  const actions = React.useMemo(() => new TabsReducerDispatch(dispatch), [dispatch]);
  
  return (<TabsContext.Provider key={appId} value={{ session, actions }}>
      {children}
    </TabsContext.Provider>);
}

const useTabs = () => {
  const result: BurgerApi.TabsContextType = React.useContext(TabsContext);
  return result;
}


export { TabsProvider, useTabs };