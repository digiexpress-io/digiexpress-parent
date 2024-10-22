import React, { createContext, PropsWithChildren, useContext, useEffect, useState } from 'react';
import { useFetch } from '../hooks/useFetch';

export interface Config {
  loaded: boolean;
  error?: Error;
  api?: string;
  tasksApiUrl?: string;
  dialobApiUrl?: string;
  dialobComposerUrl?: string;
  dialobSessionUrl?: string;
  wrenchApiUrl?: string;
  wrenchIdeUrl?: string;
  feedbackKey?: string;
  taskDeleteGroups?: string[];
  taskAdminGroups?: string[];
  appVersion?: string;
  contentRepositoryUrl?: string;
  calendarUrl?: string;
  modifiableAssets?: boolean;
}

const INITIAL_CONFIG: Config = {
  loaded: false
};

export interface ConfigContextProviderProps {
  path: string;
}

export const ConfigContext = createContext<Config>(INITIAL_CONFIG);

export const ConfigContextProvider: React.FC<PropsWithChildren<ConfigContextProviderProps>> = 
({path, children}) => {
  const [config, setConfig] = useState<Config>({ loaded: false });
  const { response } = useFetch<Config>(path);

  useEffect(() => {
    if (response) {
     setConfig({...response, loaded: true});
    }
  }, [response]);

  return (
    <ConfigContext.Provider value={config}>
      {config.loaded && children}
    </ConfigContext.Provider>
  );
};

export const useConfig = () => useContext(ConfigContext);
