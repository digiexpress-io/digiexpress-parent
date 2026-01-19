import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { CockpitApi } from '@dxs-ts/cockpit-api';
import { useCockpitsBackend } from '@dxs-ts/cockpit-api';

export interface CockpitProviderContextType {
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
  const { data: cockpitContainer, error, refetch, isPending } = useQuery({
    queryKey: [COCKPIT_QUERY_KEY, cockpitId],
    queryFn: () => backend.persistence.getOneCockpit(cockpitId)
  });

  const contextValue: CockpitProviderContextType | undefined = React.useMemo(() => {
    if(isPending || !cockpitContainer) {
      return undefined
    }

    const tenants: CockpitProviderContextType['tenants'] = {
      stencil: cockpitContainer.tenants.find(tenant => tenant.cockpitConfigTenantType === 'STENCIL'),
      wrench: cockpitContainer.tenants.find(tenant => tenant.cockpitConfigTenantType === 'WRENCH'),
    };

    return { cockpitContainer, tenants };
  }, [cockpitId, backend, cockpitContainer, isPending]);

  if(contextValue) {
    return (<CockpitProviderContext.Provider value={contextValue}>{props.children}</CockpitProviderContext.Provider>);
  }
  return (<></>);
}

export function useCockpit() {
  const result = React.useContext(CockpitProviderContext);
  if(!result) {
    throw new Error('CockpitProviderContext is not created!');
  }
  return result;
}