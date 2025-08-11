import { useFetch, useFetchConfig } from '@dxs-ts/envir-fetch';
import React, { createContext, PropsWithChildren, useContext } from 'react';


export interface Config {
  dialobComposerUrl?: string; // location of the DIALOB FORM COMPOSER UI

  // TODO:: will be deleted
  taskDeleteGroups?: string[];
    // TODO:: will be deleted
  taskAdminGroups?: string[];
  // TODO:: will be deleted
  serviceUrl: string;
  loginUrl: string;
  logoutUrl: string;
}

const INITIAL_CONFIG: Config = {
  loginUrl: '',
  logoutUrl: '',
  serviceUrl: '/',
};

export interface ConfigContextProviderProps {
  loginUrl: string; // default - unless overriden by service
  logoutUrl: string; // default - unless overriden by service
}

export const ConfigContext = createContext<Config>(INITIAL_CONFIG);

export const ConfigContextProvider: React.FC<PropsWithChildren<ConfigContextProviderProps>> = ({ children, loginUrl, logoutUrl }) => {
  const {config, pending} = useFetch('config.GET', {});
  const { setContextPath } = useFetchConfig();


  const contextValue: Config = React.useMemo(() => {
    return {
      ...config,
      loginUrl: config?.loginUrl ?? loginUrl,
      logoutUrl: config?.logoutUrl ?? logoutUrl,
      serviceUrl: config?.serviceUrl ?? '/',
    }
  }, [config]);

  React.useEffect(() => {
    if(pending || !config?.serviceUrl) {
      return;
    }
    setContextPath(config.serviceUrl);
  }, [pending, config?.serviceUrl]);

  return (
    <ConfigContext.Provider value={contextValue}>
      {!pending && children}
    </ConfigContext.Provider>
  );
};

export const useConfig = () => useContext(ConfigContext);
