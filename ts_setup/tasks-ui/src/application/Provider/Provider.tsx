import React from 'react';

import { initSession, SessionData, ActionsImpl } from 'context';
import { Backend } from 'client';
import { ClientContext, ComposerContext } from 'context/client-ctx';

import { UserProfileAndOrg } from 'descriptor-access-mgmt';
import { AmProvider } from 'descriptor-access-mgmt';
import LoggerFactory from 'logger';


const log = LoggerFactory.getLogger();

const Provider: React.FC<{ children: React.ReactNode, service: Backend, profile: UserProfileAndOrg }> = ({ children, service, profile }) => {
  const [session, dispatch] = React.useState<SessionData>(initSession);

  const actions = React.useMemo(() => {
    log.debug("init ide dispatch");
    return new ActionsImpl(dispatch);
  }, [dispatch]);

  const contextValue = React.useMemo(() => {
    log.debug("init ide context value");
    return { session, actions };
  }, [session, actions]);

  return (
    <ClientContext.Provider value={service}>
      <ComposerContext.Provider value={contextValue}>
        <AmProvider backend={service} profile={profile}>
          {children}
        </AmProvider>
      </ComposerContext.Provider>
    </ClientContext.Provider>);
};
export default Provider;
