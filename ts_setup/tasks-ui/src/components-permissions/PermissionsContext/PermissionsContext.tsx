import React from 'react';

import Context from 'context';
import { SingleTabInit } from 'descriptor-tabbing';

import { ImmutableSysConfigStore } from 'descriptor-sys-config';
import { Role } from 'descriptor-permissions';
import { TabTypes, Tabbing, PermissionsContextType } from './permissions-context-types';
import { testRoles } from './permissions-mock-data';

const PermissionsTabbingProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  function initTabs(): Record<TabTypes, SingleTabInit<{}>> {
    return {
      role_create: { body: {}, active: false },
      permission_create: { body: {}, active: false },

      role_parent: { body: {}, active: true },
      role_permissions: { body: {}, active: false },
      role_members: { body: {}, active: false },
    };
  }
  return (
    <Tabbing.Provider init={initTabs()}>
      <>{children}</>
    </Tabbing.Provider>
  );
}

export function usePermissions() {
  const tabbing = Tabbing.hooks.useTabbing();
  const activeTab = tabbing.getActiveTab();
  const { roles } = React.useContext(PermissionsContext);

  function setActiveTab(tabId: TabTypes) {
    tabbing.withTabActivity(tabId);
  }

  return {
    activeTab,
    setActiveTab,
    roles,
  };
}


const PermissionsContext = React.createContext<PermissionsContextType>({} as any);

export const PermissionsProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const backend = Context.useBackend();
  const [loading, setLoading] = React.useState(true);
  const [roles, setRoles] = React.useState<Role[]>(testRoles);
  const [store] = React.useState(new ImmutableSysConfigStore(backend.store));

  async function loadOneConfig(): Promise<void> {
    return store
      .findAllSysConfigs().then(allConfigs => {
        setLoading(false);
        if (allConfigs.length === 1) {
          setRoles(testRoles);
        }
      })
      .catch(() => setLoading(false));
  }

  // perform init
  React.useEffect(() => {
    loadOneConfig();
  }, []);

  const contextValue: PermissionsContextType = React.useMemo(() => {
    function reload(): Promise<void> {
      setLoading(true);
      return loadOneConfig();
    }
    return { loading, reload, roles };
  }, [loading, store, roles]);


  return (<PermissionsContext.Provider value={contextValue}>
    <PermissionsTabbingProvider>
      {children}
    </PermissionsTabbingProvider>
  </PermissionsContext.Provider>);
}