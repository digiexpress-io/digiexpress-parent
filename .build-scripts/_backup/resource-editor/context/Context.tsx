import React from 'react';

import { Hdes } from '../deps';
import Session from './Session';
import SessionData from './SessionData';
import { Reducer, ReducerDispatch } from './Reducer';


const ResourceContext = React.createContext<Session.ContextType>({
  service: {} as Hdes.Service,
  actions: {} as Session.Actions,
  session: {} as Session.Instance,
  theme: 'dark'
});

const sessionData = new SessionData({});

const ResourceProvider: React.FC<{children: React.ReactNode, service: Hdes.Service, theme: 'light' | 'dark'}> = ({ children, service, theme }) => {
  
  const [loaded, setLoaded] = React.useState(false);
  const [session, dispatch] = React.useReducer(Reducer, sessionData);
  const actions = React.useMemo(() => {
    console.log("init resource dispatch");
    return new ReducerDispatch(service, dispatch)
  }, [service, dispatch]);

  React.useEffect(() => {
    if(!loaded) {
      actions.getModels().then(() => setLoaded(true));  
    }
  }, [loaded, actions]) 

  return (
    <ResourceContext.Provider value={{ session, actions, service, theme }}>
      {children}
    </ResourceContext.Provider>
  );
};



const useContext = () => {
  const result: Session.ContextType = React.useContext(ResourceContext);
  return result;
}

export { ResourceProvider, ResourceContext, useContext };