import React from 'react';

import Context from 'context';
import { SingleTabInit } from 'descriptor-tabbing';

import { SysConfig, ImmutableSysConfigStore } from 'descriptor-sys-config';
import { TabTypes, Tabbing, PermissionsContextType } from './permissions-context-types';

const PermissionsTabbingProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  function initTabs(): Record<TabTypes, SingleTabInit<{}>> {
    return {
      role_create: { body: {}, active: true },
      permission_create: { body: {}, active: false }
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
  const { permissions } = React.useContext(PermissionsContext);

  function setActiveTab(tabId: TabTypes) {
    tabbing.withTabActivity(tabId);
  }

  return {
    activeTab,
    setActiveTab,
    permissions,
  };
}

const PermissionsContext = React.createContext<PermissionsContextType>({} as any);

export const PermissionsProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const backend = Context.useBackend();
  const [loading, setLoading] = React.useState(true);
  const [permissions, setPermissions] = React.useState<SysConfig>();
  const [store] = React.useState(new ImmutableSysConfigStore(backend.store));

  async function loadOneConfig(): Promise<void> {
    return store
      .findAllSysConfigs().then(allConfigs => {
        setLoading(false);
        if (allConfigs.length === 1) {
          setPermissions(allConfigs[0]);
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
    return { loading, reload, permissions };
  }, [loading, store, permissions]);


  return (<PermissionsContext.Provider value={contextValue}>
    <PermissionsTabbingProvider>
      {children}
    </PermissionsTabbingProvider>
  </PermissionsContext.Provider>);
}