import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { CockpitApi } from './cockpit-types';
import { useCockpitsBackend } from './cockpit-backend'

export interface CockpitProviderContextType {
  activity: CockpitApi.CockpitActivity,
  cockpitContainer: CockpitApi.CockpitContainer;
  tenants: {
    wrench: CockpitApi.CockpitConfigTenant | undefined;
    stencil: CockpitApi.CockpitConfigTenant | undefined;
  }
}

export const CockpitProviderContext = React.createContext<CockpitProviderContextType | undefined>(undefined);

export interface CockpitProviderProps {
  cockpitId: string;
  children: React.ReactNode
}

export const COCKPIT_QUERY_KEY = 'get-one-cockpit';
export const CockpitProvider: React.FC<CockpitProviderProps> = (props) => {
  const { cockpitId } = props;
  const backend = useCockpitsBackend();
  const containerQuery = useQuery({
    queryKey: [COCKPIT_QUERY_KEY, cockpitId],
    queryFn: () => backend.persistence.getOneCockpit(cockpitId)
  });


  const activityQuery = useQuery({
    queryKey: [COCKPIT_QUERY_KEY, cockpitId, 'activity'],
    queryFn: () => backend.persistence.findActivity()
  });


  const contextValue: CockpitProviderContextType | undefined = React.useMemo(() => {
    if (containerQuery.isPending || !containerQuery.data || !activityQuery.data) {
      return undefined
    }

    const tenants: CockpitProviderContextType['tenants'] = {
      stencil: containerQuery.data.tenants.find(tenant => tenant.cockpitConfigTenantType === 'STENCIL'),
      wrench: containerQuery.data.tenants.find(tenant => tenant.cockpitConfigTenantType === 'WRENCH'),
    };

    return { cockpitContainer: containerQuery.data, activity: activityQuery.data, tenants };
  }, [cockpitId, backend, containerQuery.isPending, activityQuery.isPending]);

  if (contextValue) {
    return (<CockpitProviderContext.Provider value={contextValue}>{props.children}</CockpitProviderContext.Provider>);
  }
  return (<></>);
}

export function useCockpit() {
  const result = React.useContext(CockpitProviderContext);
  if (!result) {
    throw new Error('CockpitProviderContext is not created!');
  }
  return result;
}