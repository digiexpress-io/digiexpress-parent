import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { CockpitApi } from './cockpit-types';
import { useCockpitsBackend } from './cockpit-backend'

export interface CockpitProviderContextType {
  cockpitContainer: CockpitApi.CockpitSummary;
  refresh: () => Promise<void>;
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



  const contextValue: CockpitProviderContextType | undefined = React.useMemo(() => {
    if (containerQuery.isPending || !containerQuery.data) {
      return undefined
    }

    return { 
      cockpitContainer: containerQuery.data, 
      refresh: async () => containerQuery.refetch().then(_ignore => {return;})
    };
  }, [cockpitId, backend, containerQuery.dataUpdatedAt]);

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