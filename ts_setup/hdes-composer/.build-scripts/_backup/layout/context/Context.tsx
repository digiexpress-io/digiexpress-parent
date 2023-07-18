import React from 'react';

import Session from './Session';
import SessionData from './SessionData';
import { Reducer, ReducerDispatch } from './Reducer';
import { Components } from './Components';


const LayoutContext = React.createContext<Session.ContextType>({
  session: {} as Session.Instance,
  actions: {} as Session.Actions,
});

const sessionData = new SessionData({
  dimensions: { width: 0, height: 0 },
  drawer: { open: false, width: 0 }
});

const LayoutProvider: React.FC<{
  styles: { drawer: { width: number } },
  config: Session.Configuration
}> = (props) => {

  const [session, dispatch] = React.useReducer(Reducer, sessionData.withDrawer({ open: false, width: props.styles.drawer.width }));
  const actions = React.useMemo(() => new ReducerDispatch(dispatch), [dispatch]);

  return (
    <LayoutContext.Provider value={{ session, actions }}>
      <Components children={props.config} />
    </LayoutContext.Provider>
  );
};

const useLayout = () => {
  const result: Session.ContextType = React.useContext(LayoutContext);
  return result;
}

export { LayoutProvider, LayoutContext, useLayout };