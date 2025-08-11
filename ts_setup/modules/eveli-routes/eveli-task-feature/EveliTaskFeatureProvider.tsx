
import { TaskApi } from '@dxs-ts/eveli-api';
import React from 'react';

export interface EveliTaskFeatureContextType {
  features: TaskApi.TaskFeatureType[]
}

export const EveliTaskFeatureContext = React.createContext<EveliTaskFeatureContextType>({} as any);

export interface EveliTaskFeatureProviderProps {
  children: React.ReactNode;
  options: {
    features?: TaskApi.TaskFeatureType[]
  } | undefined | null

}

export const EveliTaskFeatureProvider: React.FC<EveliTaskFeatureProviderProps> = (props) => {
  const { options } = props;

  const contextValue: EveliTaskFeatureContextType = React.useMemo(() => {
  return { 
    features: [ 
      ...(options?.features ?? []),
    ] 
  };    
  }, [options]);

  return (<EveliTaskFeatureContext.Provider value={contextValue}>{props.children}</EveliTaskFeatureContext.Provider>);
}

export function useTaskFeatures() {
  const result: EveliTaskFeatureContextType = React.useContext(EveliTaskFeatureContext);
  return result;
}
