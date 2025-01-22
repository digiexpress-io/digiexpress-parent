import React from 'react';
import { QueueApi } from './queue-types';

export interface QueueContextType {
  getOneChannelConfig: () => Promise<QueueApi.ChannelConfig>;
}

export const QueueContext = React.createContext<QueueContextType>({} as any);

export interface QueueProviderProps {
  children: React.ReactNode;
  fetchQueueConfigGET: QueueApi.FetchQueueConfigGET;
}
export const QueueProvider: React.FC<QueueProviderProps> = (props) => {

  // create the context 
  const contextValue: QueueContextType = React.useMemo(() => {

    function getOneChannelConfig(): Promise<QueueApi.ChannelConfig> {
      return props.fetchQueueConfigGET()
        .then(resp => resp.json())
        .then(data => data as QueueApi.ChannelConfig[])
        .then(([data]) => data);
    }


    // return all methods
    return {
      getOneChannelConfig, 
    };
  }, [
   props.fetchQueueConfigGET
  ]);

  return (<QueueContext.Provider value={contextValue}>{props.children}</QueueContext.Provider>);
}

export function useQueue() {
  const result: QueueContextType = React.useContext(QueueContext);
  return result;
}
