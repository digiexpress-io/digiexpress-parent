import React from 'react';

import Backend from 'descriptor-backend';
import { SingleTabInit } from 'descriptor-tabbing';

import HdesClient from 'components-hdes/core';
import { StencilClient } from 'components-stencil';

import { SysConfig, ImmutableSysConfigStore } from 'descriptor-sys-config';


import { TabTypes, Tabbing, SysConfigContextType } from './sys-config-context-types';



const SysConfigTabbingProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  function initTabs(): Record<TabTypes, SingleTabInit<{}>> {
    return {
      current_config: { body: {}, active: true },
      all_config: { body: {}, active: false }
    };
  }
  return (
    <Tabbing.Provider init={initTabs()}>
      <>{children}</>
    </Tabbing.Provider>
  );
}

export function useSysConfig() {
  const tabbing = Tabbing.hooks.useTabbing();
  const activeTab = tabbing.getActiveTab();
  const { sysConfig, hdesSite, stencilSite } = React.useContext(SysConfigContext);

  function setActiveTab(tabId: TabTypes) {
    tabbing.withTabActivity(tabId);
  }

  return {
    activeTab,
    setActiveTab,
    sysConfig,
    hdesSite,
    stencilSite
  };
}

const SysConfigContext = React.createContext<SysConfigContextType>({} as any);




export const SysConfigProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const backend = Backend.useBackend();
  const [loading, setLoading] = React.useState(true);
  const [sysConfig, setSysConfig] = React.useState<SysConfig>();
  const [hdesSite, setHdesSite] = React.useState<HdesClient.Site>();
  const [stencilSite, setStencilSite] = React.useState<StencilClient.Release>();
  const [store] = React.useState(new ImmutableSysConfigStore(backend.store));

  const sysConfigId = sysConfig?.id;

  async function loadOneConfig(): Promise<void> {
    return store
      .findAllSysConfigs().then(allConfigs => {
        setLoading(false);
        if (allConfigs.length === 1) {
          setSysConfig(allConfigs[0]);
        }
      })
      .catch(() => setLoading(false));
  }


  async function loadHdesSite(id: string): Promise<void> {
    store.getHdesSiteFromSysConfig(id).then(setHdesSite);
  }

  async function loadStencilSite(id: string): Promise<void> {
    store.getStencilSiteFromSysConfig(id).then(setStencilSite);
  }

  // perform init
  React.useEffect(() => {
    loadOneConfig();
  }, []);


  React.useEffect(() => {
    if (sysConfigId) {
      loadHdesSite(sysConfigId);
    }
  }, [sysConfigId]);

  React.useEffect(() => {
    if (sysConfigId) {
      loadStencilSite(sysConfigId);
    }
  }, [sysConfigId]);


  const contextValue: SysConfigContextType = React.useMemo(() => {
    function reload(): Promise<void> {
      setLoading(true);
      return loadOneConfig();
    }
    return { loading, reload, sysConfig, hdesSite, stencilSite };
  }, [loading, store, sysConfig, hdesSite, stencilSite]);


  return (<SysConfigContext.Provider value={contextValue}>
    <SysConfigTabbingProvider>
      {children}
    </SysConfigTabbingProvider>
  </SysConfigContext.Provider>);
}