import React from 'react';

import { Backend } from 'client';
import { UserProfileAndOrg } from 'descriptor-access-mgmt';


import { TenantContextType, TenantEntry, Tenant, TenantId } from './types';
import { ImmutableTenantState } from './ImmutableTenantState';
import { ImmutableTenantStore } from './ImmutableTenantStore';


import LoggerFactory from 'logger';
const log = LoggerFactory.getLogger();



const initState: ImmutableTenantState = new ImmutableTenantState({ activeTenant: undefined, tenantEntries: [], tenants: [] });
type WithActiveTenant = (tenantId?: TenantId) => void;
type WithTenants = (tenants: Tenant[]) => void;
type WithTenantEntries = (tenantEntries: TenantEntry[]) => void;


export const TenantContext = React.createContext<TenantContextType>({} as TenantContextType);

export const TenantProvider: React.FC<{ children: React.ReactNode, 
  init: {
    backend: Backend;
    profile: UserProfileAndOrg;
  } }> = ({ children, init }) => {
  const { backend } = init;

  const [loading, setLoading] = React.useState<boolean>(true);
  const [state, setState] = React.useState<ImmutableTenantState>(initState);

  const withTenants: WithTenants = React.useCallback((tenants) => setState(prev => prev.withTenants(tenants)), [setState]);
  const withActiveTenant: WithActiveTenant = React.useCallback((tenantId) => setState(prev => prev.withActiveTenant(tenantId)), [setState]);
  const withTenantEntries: WithTenantEntries = React.useCallback((tenantEntries) => setState(prev => prev.withTenantEntries(tenantEntries)), [setState]);


  const contextValue: TenantContextType = React.useMemo(() => {
    async function reload() {
      return new ImmutableTenantStore(backend.store).getTenantEntries(state.activeTenant ?? '').then(data => {
        log.debug('reload active tenant', state.activeTenant);
        return setState(prev => prev.withTenantEntries(data.records))
      });
    }

    return { state, loading, reload, withActiveTenant, withTenants, withTenantEntries };
  }, [state, loading, backend, withActiveTenant, withTenants, withTenantEntries]);

  React.useEffect(() => {
    if (!loading) {
      return;
    }
    new ImmutableTenantStore(backend.store).getTenants().then(data => {
      setLoading(false);
      log.debug("loaded tenant", data);

      if (data.length === 1) {
        const [{ id }] = data;
        new ImmutableTenantStore(backend.store).getTenantEntries(id).then(entries => {
          setState(prev => prev.withActiveTenant(id).withTenants(data).withTenantEntries(entries.records));
        });
      }
    });

  }, [loading, setLoading, backend]);


  return (<TenantContext.Provider value={contextValue}>{children}</TenantContext.Provider>);
};
