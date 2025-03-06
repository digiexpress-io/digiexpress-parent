import { useFetch } from '@dxs-ts/eveli-fetch';
import React, { createContext, PropsWithChildren, useContext } from 'react';


export interface Config {
  dialobComposerUrl?: string; // location of the DIALOB FORM COMPOSER UI
  taskDeleteGroups?: string[];
  taskAdminGroups?: string[];
  appVersion?: string;
  modifiableAssets?: boolean; //enable releases and other asset operations

  loginUrl: string;
  logoutUrl: string;
}

const INITIAL_CONFIG: Config = {
  loginUrl: '',
  logoutUrl: ''
};

export interface ConfigContextProviderProps {
  loginUrl: string; // default - unless overriden by service
  logoutUrl: string; // default - unless overriden by service
}

export const ConfigContext = createContext<Config>(INITIAL_CONFIG);

export const ConfigContextProvider: React.FC<PropsWithChildren<ConfigContextProviderProps>> = ({ children, loginUrl, logoutUrl }) => {
  const {config, pending} = useFetch('config.GET', {}); 

  const contextValue: Config = React.useMemo(() => {
    return {
      ...config,      
      loginUrl: config?.loginUrl ?? loginUrl,
      logoutUrl: config?.logoutUrl ?? logoutUrl,
    }
  }, [config]);

  return (
    <ConfigContext.Provider value={contextValue}>
      {!pending && children}
    </ConfigContext.Provider>
  );
};

export const useConfig = () => useContext(ConfigContext);
