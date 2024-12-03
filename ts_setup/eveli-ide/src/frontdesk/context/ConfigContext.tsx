import React, { createContext, PropsWithChildren, useContext, useEffect, useState } from 'react';
import { useFetch } from '../hooks/useFetch';

export interface Config {
  serviceUrl?: string;
  dialobComposerUrl?: string;
  taskDeleteGroups?: string[];
  taskAdminGroups?: string[];
  appVersion?: string;
  modifiableAssets?: boolean; //enable releases and other asset operations
}

const INITIAL_CONFIG: Config = {};

export interface ConfigContextProviderProps {
  path: string;
}

export const ConfigContext = createContext<Config>(INITIAL_CONFIG);

export const ConfigContextProvider: React.FC<PropsWithChildren<ConfigContextProviderProps>> = 
({path, children}) => {

  const [pending, setPending] = useState<boolean>(true);
  const [config, setConfig] = useState<Config>({});
  const { response } = useFetch<Config>(path);

  useEffect(() => {
    if (response) {
      setConfig({ ...response });
      setPending(false);
    }
  }, [response]);

  return (
    <ConfigContext.Provider value={config}>
      {!pending && children}
    </ConfigContext.Provider>
  );
};

export const useConfig = () => useContext(ConfigContext);
