import React from 'react';

import { initSession, SessionData } from './session';
import ActionsImpl from './actions';
import { Client } from './client-types';
import { Profile } from './profile-types';
import { ClientContext, ComposerContext } from './client-ctx';
import RequireProject from './Components/RequireProject';
import { TasksProvider } from './tasks-ctx';
import { OrgProvider } from './org-ctx';
import { MenuProvider } from './menu-ctx';

const Provider: React.FC<{ children: React.ReactNode, service: Client, profile: Profile }> = ({ children, service, profile }) => {
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
    if (profile) {
      actions.handleLoadProfile(profile);
    } else {
      actions.handleLoad();
    }
  }, [actions, profile]);

  return (
    <ClientContext.Provider value={service}>
      <ComposerContext.Provider value={contextValue}>
        <TasksProvider backend={service} profile={session.profile}>
          <OrgProvider backend={service}>
            <MenuProvider>
              {session.profile.contentType === 'NOT_CREATED' ? <RequireProject /> : undefined}
              {children}
            </MenuProvider>
          </OrgProvider>
        </TasksProvider>
      </ComposerContext.Provider>
    </ClientContext.Provider>);
};
export default Provider;
