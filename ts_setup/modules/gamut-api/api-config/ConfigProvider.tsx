



import React from 'react'

import { ConfigApi } from './config-types';

export const ConfigContext = React.createContext<ConfigApi.ConfigContextType>({} as any);

export const ConfigProvider: React.FC<{ children: React.ReactNode, options: ConfigApi.Options }> = (props) => {
  const contextValue: ConfigApi.ConfigContextType = React.useMemo(() => {
    return Object.freeze(ConfigApi.mergeOptions(props.options));
  }, [props.options]);

  return (<ConfigContext.Provider value={contextValue}>{props.children}</ConfigContext.Provider>); 
}

export function useConfig(): ConfigApi.ConfigContextType {
  const result: ConfigApi.ConfigContextType = React.useContext(ConfigContext);
  return result;
}

