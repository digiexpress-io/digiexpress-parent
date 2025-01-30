import React from 'react';
import { QueueApi } from './queue-types';

export interface QueueContextType {
  getOneChannelConfig: () => Promise<QueueApi.ChannelConfig>;
  findAllQueueMessages: () => Promise<QueueApi.QueueMessage[]>;
  findAllQueueDeliveries: () => Promise<QueueApi.Delivery[]>;
}

export const QueueContext = React.createContext<QueueContextType>({} as any);

export interface QueueProviderProps {
  children: React.ReactNode;
  fetchQueueConfigGET: QueueApi.FetchQueueConfigGET;
  fetchQueueMessagesGET: QueueApi.FetchQueueMessagesGET;
  fetchQueueDeliveriesGET: QueueApi.FetchQueueDeliveriesGET;
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

    function findAllQueueMessages(): Promise<QueueApi.QueueMessage[]> {
      return props.fetchQueueMessagesGET()
      .then(resp => resp.json())
      .then(data => data as QueueApi.QueueMessage[])
    }

    function findAllQueueDeliveries(): Promise<QueueApi.Delivery[]> {
      return props.fetchQueueDeliveriesGET()
      .then(resp => resp.json())
      .then(data => data as QueueApi.Delivery[])
    }

    // return all methods
    return {
      getOneChannelConfig, findAllQueueMessages, findAllQueueDeliveries
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
