import React from 'react';

import { BurgerApi } from '../../BurgerApi';
import SecondarySessionData from './SecondaryData';
import { SecondaryReducer, SecondaryReducerDispatch } from './SecondaryReducer';


const SecondaryContext = React.createContext<BurgerApi.SecondaryContextType>({
  session: {} as BurgerApi.SecondarySession,
  actions: {} as BurgerApi.SecondaryActions,
})

const sessionInit: SecondarySessionData = new SecondarySessionData({appId: ""})

const SecondaryProvider: React.FC<{appId: string, secondary?: string, children: React.ReactNode}> = ({appId, secondary, children}) => {
  const [session, dispatch] = React.useReducer(SecondaryReducer, sessionInit.withAppId(appId).withSecondary(secondary));
  const actions = React.useMemo(() => new SecondaryReducerDispatch(dispatch), [dispatch]);
  
  return (<SecondaryContext.Provider key={appId} value={{ session, actions }}>
      {children}
    </SecondaryContext.Provider>);
}

const useSecondary = () => {
  const result: BurgerApi.SecondaryContextType = React.useContext(SecondaryContext);
  return result;
}

export { SecondaryProvider, useSecondary, SecondaryContext };