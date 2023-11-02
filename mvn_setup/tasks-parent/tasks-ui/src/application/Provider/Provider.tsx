import React from 'react';

import { initSession, SessionData, ActionsImpl } from 'context';

import { Profile, Backend } from 'client';
import { ClientContext, ComposerContext } from 'context/client-ctx';
import RequireProject from './RequireProject';
import { TasksProvider } from 'context/tasks-ctx';
import { OrgProvider } from 'context/org-ctx';
import { ProjectsProvider } from 'context/projects-ctx';

const Provider: React.FC<{ children: React.ReactNode, service: Backend, profile: Profile }> = ({ children, service, profile }) => {
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
        <ProjectsProvider backend={service} profile={session.profile}>
          <TasksProvider backend={service} profile={session.profile}>
            <OrgProvider backend={service}>
              {session.profile.contentType === 'NOT_CREATED' ? <RequireProject /> : undefined}
              {children}
            </OrgProvider>
          </TasksProvider>
        </ProjectsProvider>
      </ComposerContext.Provider>
    </ClientContext.Provider>);
};
export default Provider;
