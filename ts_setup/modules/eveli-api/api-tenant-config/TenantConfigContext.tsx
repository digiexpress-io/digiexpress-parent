
import React, { createContext, PropsWithChildren, useContext } from 'react';
import { ThemeOptions } from '@mui/material';
import { useFetch } from '@dxs-ts/envir-fetch';
import { GThemeOptions } from '@dxs-ts/gamut-theme';
import { EveliFeatureMapping, EveliFeatureType } from './EveliFeatureMapping';


export interface TenantConfig {
  features: TenantFeature[];
  gamutThemeOptions: ThemeOptions;
}


export const tenant_features = [
  'wrench-only',

  "wrench-disabled",
  "stencil-disabled",

  "external-deployment",
  "smart_tables",
  "smart_task_audit",
  "user_profile",
  'queues-visually-disabled',
  'feedback-visually-disabled',
  'in_house',

  'stencil_locale_filter',
  'eveli_publication_only',
  'visual_accommodation',
  'batches',
  
  'tagomi',
  'contract',
  'ai-assistant',
  'cockpits',

  'eveli_tree',
] as const;

export type TenantFeature = typeof tenant_features[number];



const INITIAL_CONFIG: TenantConfigContextType = {
  features: [],
  gamutThemeOptions: GThemeOptions,
  hardcodedFeatures: [],
  userTenantConfig: undefined,
  setUserTenantConfig: () => {}
}

export interface TenantConfigContextProviderProps {
  disabled?: boolean;
  features?: TenantFeature[] | undefined;
  gamutThemeOptions?: ThemeOptions | undefined;
}

export interface TenantConfigContextType extends TenantConfig {
  hardcodedFeatures: TenantFeature[];
  userTenantConfig: TenantFeature[] | undefined
  setUserTenantConfig: React.Dispatch<React.SetStateAction<TenantFeature[] | undefined>>;
}

export const TenantConfigContext = createContext<TenantConfigContextType>(INITIAL_CONFIG);

const WithProvider: React.FC<PropsWithChildren<TenantConfigContextProviderProps>> = ({ children, features: _features, gamutThemeOptions }) => {
  const {tenantConfig, pending} = useFetch('worker/rest/api/tenant-configs.GET', {}); 
  const [userTenantConfig, setUserTenantConfig] = React.useState<TenantFeature[]>();
  
  const contextValue: TenantConfigContextType = React.useMemo(() => {
    if(pending) {
      return Object.freeze({ ...INITIAL_CONFIG, hardcodedFeatures: _features ?? [], setUserTenantConfig, userTenantConfig })
    }

    const hardcodedFeatures = Array.from(new Set([...(tenantConfig?.features ?? []), ...(_features ?? [])]));

    const mergedTheme = gamutThemeOptions ?? GThemeOptions;
    const features = Array.from(new Set([ ...(tenantConfig?.features ?? []), ...(_features ?? []), ...(userTenantConfig ?? []) ]));

    return Object.freeze({ gamutThemeOptions: mergedTheme, ...tenantConfig, features, hardcodedFeatures, userTenantConfig, setUserTenantConfig })
  }, [tenantConfig, pending, userTenantConfig, _features, setUserTenantConfig]);

  return (
    <TenantConfigContext.Provider value={contextValue}>
      {!pending && children}
    </TenantConfigContext.Provider>
  );
}

export const TenantConfigContextProvider: React.FC<PropsWithChildren<TenantConfigContextProviderProps>> = (props) => {
  if(props.disabled === true) {
    return (<>{props.children}</>)
  }
  return (<WithProvider {...props}/>);
}

export const useTenantConfig = () => useContext(TenantConfigContext);
export const useTenantConfigFeatures = () => {
  const context = useContext(TenantConfigContext);

  return {
    isEnabled: (id: EveliFeatureType) => {
      const required = EveliFeatureMapping[id];
      const isEnabled = required(context.features);
      return isEnabled;
    }
  }  
}


