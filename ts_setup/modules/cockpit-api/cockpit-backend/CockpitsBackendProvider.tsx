import React from 'react';
import { CockpitApi } from '../cockpit-types';

export interface CockpitsBackendContextType {
  navigate: {
    findAllCockpits: () => void;
    createOneCockpit: () => void;
  };
  persistence: {
    findAllCockpits: () => Promise<CockpitApi.CockpitSummary[]>;
    createOneCockpit: (request: CockpitApi.CreateCockpitCommand) => Promise<CockpitApi.CockpitContainer>;
  }
}

export const CockpitsBackendContext = React.createContext<CockpitsBackendContextType>({} as any);

export interface CockpitsBackendProviderProps {
  children: React.ReactNode;
  navigate: CockpitsBackendContextType['navigate'];
  persistence: CockpitsBackendContextType['persistence'];
}

export const CockpitsBackendProvider: React.FC<CockpitsBackendProviderProps> = (props) => {
  const { navigate, persistence } = props;

  const contextValue: CockpitsBackendContextType = React.useMemo(() => {
    return { navigate, persistence };
  }, [navigate, persistence]);

  return (<CockpitsBackendContext.Provider value={contextValue}>{props.children}</CockpitsBackendContext.Provider>);
}

export function useCockpitsBackend() {
  const result: CockpitsBackendContextType = React.useContext(CockpitsBackendContext);
  return result;
}