
import React from 'react';
import { TaskApi } from '../task-types';

export interface TaskFeatureContextType {
  features: TaskApi.TaskFeatureType[]
}

export const TaskFeatureContext = React.createContext<TaskFeatureContextType>({} as any);

export interface TaskFeatureProviderProps {
  children: React.ReactNode;
  options: {
    features?: TaskApi.TaskFeatureType[]
  } | undefined | null

}

export const TaskFeatureProvider: React.FC<TaskFeatureProviderProps> = (props) => {
  const { options } = props;

  const contextValue: TaskFeatureContextType = React.useMemo(() => {
  return { 
    features: [ 
      ...(options?.features ?? []),
    ] 
  };    
  }, [options]);

  return (<TaskFeatureContext.Provider value={contextValue}>{props.children}</TaskFeatureContext.Provider>);
}

export function useTaskFeatures() {
  const result: TaskFeatureContextType = React.useContext(TaskFeatureContext);
  return result;
}
