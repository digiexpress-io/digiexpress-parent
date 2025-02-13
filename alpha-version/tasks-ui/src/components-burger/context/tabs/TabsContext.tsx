import React from 'react';

import * as API from './TabsAPI';
import TabsSessionData from './TabsSessionData';
import { TabsReducer, TabsReducerDispatch } from './TabsReducer';


const TabsContext = React.createContext<API.TabsContextType>({
  session: {} as API.TabsSession,
  actions: {} as API.TabsActions,
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
  const result: API.TabsContextType = React.useContext(TabsContext);
  return result;
}


export { TabsProvider, useTabs };