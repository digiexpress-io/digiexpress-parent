import React from 'react';

import * as API from './SecondaryAPI';
import SecondarySessionData from './SecondaryData';
import { SecondaryReducer, SecondaryReducerDispatch } from './SecondaryReducer';


const SecondaryContext = React.createContext<API.SecondaryContextType>({
  session: {} as API.SecondarySession,
  actions: {} as API.SecondaryActions,
})

const sessionInit: SecondarySessionData = new SecondarySessionData({appId: ""})

const SecondaryProvider: React.FC<{appId: string, secondary?: string}> = ({appId, secondary, children}) => {
  const [session, dispatch] = React.useReducer(SecondaryReducer, sessionInit.withAppId(appId).withSecondary(secondary));
  const actions = React.useMemo(() => new SecondaryReducerDispatch(dispatch), [dispatch]);
  
  return (<SecondaryContext.Provider key={appId} value={{ session, actions }}>
      {children}
    </SecondaryContext.Provider>);
}

const useSecondary = () => {
  const result: API.SecondaryContextType = React.useContext(SecondaryContext);
  return result;
}

export { SecondaryProvider, useSecondary, SecondaryContext };