import React from 'react';
import { TenantConfigId, TenantConfig } from './tenant-config-types';

export interface TenantConfigContextType {
  tenantConfigId: TenantConfigId | undefined;
  tenantConfig: TenantConfig | undefined;
}

export const TenantConfigContext = React.createContext<TenantConfigContextType>({
  tenantConfigId: '',
  tenantConfig: {
    id: '',
    name: '',
    created: new Date(),
    updated: new Date(),
    archived: undefined,
    status: 'IN_FORCE',
    transactions: [],
    preferences: {
      landingApp: 'APP_FRONTOFFICE'
    },
    repoConfigs: [],
    documentType: 'TENANT_CONFIG',
    version: ''
  },
});


export const TenantConfigProvider: React.FC<{ children: React.ReactNode, tenantConfig: TenantConfig }> = ({ children, tenantConfig: init }) => {
  const [tenantConfig, setTenantConfig] = React.useState<TenantConfig>(init);
  const [tenantConfigId, setTenantConfigId] = React.useState<TenantConfigId>(init.id);

  const contextValue: TenantConfigContextType = React.useMemo(() => {
    return {
      tenantConfigId, tenantConfig, setTenantConfigId
    };
  }, [tenantConfigId, tenantConfig]);

  return (<TenantConfigContext.Provider value={contextValue}>{children}</TenantConfigContext.Provider>);
}