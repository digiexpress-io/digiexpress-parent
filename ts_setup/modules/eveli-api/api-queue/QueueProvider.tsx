import React from 'react';
import { useFetch } from '@dxs-ts/envir-fetch';
import { QueueApi } from './queue-types';

export interface QueueContextType {
  getOneChannelConfig: () => Promise<QueueApi.ChannelConfig>;
  findAllQueueMessages: () => Promise<QueueApi.QueueMessage[]>;
  findAllQueueDeliveries: () => Promise<QueueApi.Delivery[]>;
}

export const QueueContext = React.createContext<QueueContextType>({} as any);

export interface QueueProviderProps {
  children: React.ReactNode;
}
export const QueueProvider: React.FC<QueueProviderProps> = (props) => {
  const { getOneChannelConfig } = useFetch('worker/rest/api/queues/configs.GET', {});
  const { findAllQueueMessages } = useFetch('worker/rest/api/queues/messages.GET', {});
  const { findAllQueueDeliveries } = useFetch('worker/rest/api/queues/deliveries.GET', {});

  // create the context 
  const contextValue: QueueContextType = React.useMemo(() => {
    return { getOneChannelConfig, findAllQueueMessages, findAllQueueDeliveries };
  }, [ getOneChannelConfig, findAllQueueMessages, findAllQueueDeliveries ]);

  return (<QueueContext.Provider value={contextValue}>{props.children}</QueueContext.Provider>);
}

export function useQueue() {
  const result: QueueContextType = React.useContext(QueueContext);
  return result;
}
