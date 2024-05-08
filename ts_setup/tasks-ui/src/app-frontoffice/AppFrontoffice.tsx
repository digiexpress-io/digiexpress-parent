import React from 'react';


import Burger from 'components-burger';
import { UserProfileAndOrg } from 'descriptor-access-mgmt';
import { Backend, useBackend } from 'descriptor-backend';
import AppStencil from 'app-stencil';
import AppHdes from 'app-hdes';


import Views from './Views';
import { FrontofficePrefs, SyncDrawer, InitNav, useDrawerOpen } from './FrontofficePrefs';
import { useProfile } from 'descriptor-backend/backend-ctx';


const AppConfigProvider: React.FC<{ children: React.ReactNode, init: { backend: Backend, profile: UserProfileAndOrg } }> = ({ children, init }) => {
  return (
    <>
      <SyncDrawer />
      {children}
    </>);
}


function AppConfig(backend: Backend, profile: UserProfileAndOrg): Burger.App<{}, { backend: Backend, profile: UserProfileAndOrg }> {
  return {
    id: "app-frontoffice",
    init: { backend, profile },
    components: {
      primary: Views.Main,
      secondary: Views.Secondary,
      toolbar: Views.Toolbar,
      context: AppConfigProvider,
      tabs: false
    },
    state: [
      (children: React.ReactNode, _restorePoint?: Burger.AppState<{}>) => (<>{children}<InitNav /></>),
      () => ({})
    ]
  }
}

const NestedApps: React.FC<{  }> = ({ }) => {
  const backend = useBackend();
  const profile = useProfile();
  const tenantConfig = profile.tenant;
  const drawerOpen = useDrawerOpen();

  const hdes: Burger.App<{}, any> = React.useMemo(() => AppHdes(backend, profile, tenantConfig!), [backend, profile, tenantConfig]);
  const stencil: Burger.App<{}, any> = React.useMemo(() => AppStencil(backend, profile, tenantConfig!), [backend, profile, tenantConfig]);
  const frontoffice: Burger.App<{}, any> = React.useMemo(() => AppConfig(backend, profile), [backend, profile]);
  const appId = tenantConfig.preferences.landingApp;
  return (<Burger.Provider secondary="toolbar.activities" drawerOpen={drawerOpen} appId={appId} children={[stencil, hdes, frontoffice]} />)
}

export const AppFrontoffice: React.FC<{  }> = ({  }) => {  
  return (
    <FrontofficePrefs>
      <NestedApps />
    </FrontofficePrefs>
  );
}


