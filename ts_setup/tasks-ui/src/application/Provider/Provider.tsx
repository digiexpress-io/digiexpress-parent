import React from 'react';

import { initSession, SessionData, ActionsImpl } from 'context';

import { UserProfileAndOrg, Backend } from 'client';
import { ClientContext, ComposerContext } from 'context/client-ctx';
import { OrgProvider } from 'descriptor-organization';
import LoggerFactory from 'logger';

const log = LoggerFactory.getLogger();

const Provider: React.FC<{ children: React.ReactNode, service: Backend, profile: UserProfileAndOrg }> = ({ children, service, profile }) => {
  const [session, dispatch] = React.useState<SessionData>(initSession);

  const actions = React.useMemo(() => {
    log.debug("init ide dispatch");
    return new ActionsImpl(dispatch, service);
  }, [dispatch, service]);

  const contextValue = React.useMemo(() => {
    log.debug("init ide context value");
    return { session, actions };
  }, [session, actions]);

  React.useLayoutEffect(() => {
    log.debug("init ide data");
    if (profile) {
      actions.handleLoadProfile(profile);
    } else {
      actions.handleLoad();
    }
  }, [actions, profile]);

  return (
    <ClientContext.Provider value={service}>
      <ComposerContext.Provider value={contextValue}>
        <OrgProvider backend={service} profile={profile}>
          {children}
        </OrgProvider>
      </ComposerContext.Provider>
    </ClientContext.Provider>);
};
export default Provider;
