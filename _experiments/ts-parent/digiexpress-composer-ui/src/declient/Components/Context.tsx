import React from 'react';

import { initSession, SessionData } from '../session';
import ActionsImpl from '../actions';
import { Client, HeadState } from '../client-types';
import { Session, Actions } from '../composer-types';
import RequireProject from './RequireProject';

export interface ComposerContextType {
  session: Session;
  actions: Actions;
}

export type ClientContextType = Client;

export const ComposerContext = React.createContext<ComposerContextType>({
  session: {} as Session,
  actions: {} as Actions,
});
export const ClientContext = React.createContext<ClientContextType>({} as ClientContextType);

export const Provider: React.FC<{ children: React.ReactNode, service: Client, head?: HeadState }> = ({ children, service, head }) => {
  const [session, dispatch] = React.useState<SessionData>(initSession);

  const actions = React.useMemo(() => {
    console.log("init ide dispatch");
    return new ActionsImpl(dispatch, service);
  }, [dispatch, service]);

  const contextValue = React.useMemo(() => {
    console.log("init ide context value");
    return { session, actions };
  }, [session, actions]);

  React.useLayoutEffect(() => {
    console.log("init ide data");
    if (head) {
      actions.handleLoadHead(head);
    } else {
      actions.handleLoad();
    }
  }, [actions, head]);

  return (
    <ClientContext.Provider value={service}>
      <ComposerContext.Provider value={contextValue}>
        {session.head.contentType === 'NOT_CREATED' ? <RequireProject /> : undefined}
        {children}
      </ComposerContext.Provider>
    </ClientContext.Provider>);
};

