import React from 'react';

import { Hdes } from '../../deps';
import Resource from '../../';
import Session from './Session';
import SessionData from './SessionData';
import { Reducer, ReducerDispatch } from './Reducer';


const DebugContext = React.createContext<Session.ContextType>({
  service: {} as Hdes.Service,
  actions: {} as Session.Actions,
  session: {} as Session.Instance,
  active: {} as Session.Active
});

const sessionData = new SessionData({ models: {}, inputs: true, outputs: false });

const DebugProvider: React.FC<{children: React.ReactNode}> = ({ children }) => {
  
  const resource = Resource.useContext();
  const service = resource.service;
  const [session, dispatch] = React.useReducer(Reducer, sessionData);
  
  const actions = React.useMemo(() => {
    console.log("init debug dispatch");
    return new ReducerDispatch(service, dispatch)
  }, [service, dispatch]);


  const getActive = (): Session.Active | undefined => {
    const active = session.active;
    if (!active) {
      return undefined;
    }

    const debug = session.getModel(active);
    if (!debug) {
      return undefined;
    }

    const debugData = debug.model ? resource.session.getModel(debug.model) : undefined;
    if (!debugData || !debug.model) {
      return undefined;
    }
    try {
      const result = resource.session.getModel(debug.model);
      return { debug: debug, model: result }
    } catch (e) {
      return undefined;
    }
  }

  const active = getActive();
  return (
    <DebugContext.Provider value={{ session, actions, service, active }}>
      {children}
    </DebugContext.Provider>
  );
};


const useContext = () => {
  const result: Session.ContextType = React.useContext(DebugContext);
  return result;
}

export { DebugProvider, DebugContext, useContext };