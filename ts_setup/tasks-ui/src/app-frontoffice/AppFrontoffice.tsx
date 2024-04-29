import React from 'react';


import Burger from 'components-burger';
import { UserProfileAndOrg, TenantConfig, AccessMgmtContextProvider } from 'descriptor-access-mgmt';
import { Backend, useBackend } from 'descriptor-backend';
import { TasksProvider } from 'descriptor-task';
import { TenantProvider } from 'descriptor-dialob';
import { AvatarProvider } from 'descriptor-avatar';
import AppStencil from 'app-stencil';
import AppHdes from 'app-hdes';


import Views from './Views';
import { getDrawerOpen, FrontofficePrefs, SyncDrawer, InitNav } from './FrontofficePrefs';


const AppConfigProvider: React.FC<{ children: React.ReactNode, init: { backend: Backend, profile: UserProfileAndOrg } }> = ({ children, init }) => {
  return (
    <TasksProvider init={init}>
      <TenantProvider init={init}>
        <AvatarProvider>
          <>
            <SyncDrawer />
            {children}
          </>
        </AvatarProvider>
      </TenantProvider>
    </TasksProvider>);
}


function AppConfig(backend: Backend, profile: UserProfileAndOrg): Burger.App<{}, { backend: Backend, profile: UserProfileAndOrg }> {
  return {
    id: "app-frontoffice",
    init: { backend, profile },
    components: {
      primary: Views.Main,
      secondary: Views.Secondary,
      toolbar: Views.Toolbar,
      context: AppConfigProvider
    },
    state: [
      (children: React.ReactNode, _restorePoint?: Burger.AppState<{}>) => (<>{children}<InitNav /></>),
      () => ({})
    ]
  }
}

const NestedApps: React.FC<{ profile: UserProfileAndOrg, tenantConfig: TenantConfig }> = ({ profile, tenantConfig }) => {
  const backend = useBackend();
  const drawerOpen = getDrawerOpen(profile);

  const hdes: Burger.App<{}, any> = React.useMemo(() => AppHdes(backend, profile, tenantConfig!), [backend, profile, tenantConfig]);
  const stencil: Burger.App<{}, any> = React.useMemo(() => AppStencil(backend, profile, tenantConfig!), [backend, profile, tenantConfig]);
  const frontoffice: Burger.App<{}, any> = React.useMemo(() => AppConfig(backend, profile), [backend, profile]);
  const appId = tenantConfig.preferences.landingApp;
  return (<Burger.Provider secondary="toolbar.activities" drawerOpen={drawerOpen} appId={appId} children={[ stencil, hdes, frontoffice ]} />)
}

export const AppFrontoffice: React.FC<{ profile: UserProfileAndOrg }> = ({ profile }) => {
  const { tenant } = profile;

  return (
    <AccessMgmtContextProvider profile={profile} tenantConfig={tenant}>
      <FrontofficePrefs>
        <NestedApps profile={profile} tenantConfig={tenant}/>
      </FrontofficePrefs>
    </AccessMgmtContextProvider>
  );
}


