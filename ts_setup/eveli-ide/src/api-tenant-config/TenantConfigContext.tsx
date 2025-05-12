import { useFetch } from '@dxs-ts/eveli-fetch';
import React, { createContext, PropsWithChildren, useContext } from 'react';


export interface TenantConfig {
  features: TenantFeature[];
}

export type TenantFeature = (
  'wrench-only' | 
  "wrench-disabled" |
  "stencil-disabled"  |
  "external-deployment" |

  'stencil_locale_filter' | 
  'eveli_publication_only' | 
  'visual_accommodation');

const INITIAL_CONFIG: TenantConfig = {
  features: []
}

export interface TenantConfigContextProviderProps {
  disabled?: boolean;
  features?: TenantFeature[] | undefined;
}

export const TenantConfigContext = createContext<TenantConfig>(INITIAL_CONFIG);

const WithProvider: React.FC<PropsWithChildren<TenantConfigContextProviderProps>> = ({ children, features: _features }) => {
  const {tenantConfig, pending} = useFetch('worker/rest/api/tenant-configs.GET', {}); 

  const contextValue: TenantConfig = React.useMemo(() => {
    return {
      ...tenantConfig,
      features: [ ...(tenantConfig?.features ?? []), ...(_features ?? []) ]
    }
  }, [tenantConfig]);


  return (
    <TenantConfigContext.Provider value={contextValue}>
      {!pending && children}
    </TenantConfigContext.Provider>
  );
};


export const TenantConfigContextProvider: React.FC<PropsWithChildren<TenantConfigContextProviderProps>> = (props) => {
  if(props.disabled === true) {
    return (<>{props.children}</>)
  }

  return (
    <WithProvider {...props}/>
  );
};




export const useTenantConfig = () => useContext(TenantConfigContext);
