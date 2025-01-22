
export namespace QueueApi {

}

export declare namespace QueueApi {
  export type FetchQueueConfigGET = () => Promise<Response>;

  export interface ChannelConfig {
    channel: Channel;
    queues: Queue[];
    queueConsumers: QueueConsumer[];
  }

  export interface Channel {
    id: string;
    prefix: string;
    externalId: string | undefined;
    createdAt: string;
    createdBy: string;
    comment: string;
    channelName: string;
  }

  export interface Queue {
    id: string;
    createdAt: string;
    createdBy: string;
    comment: string;
    queueName: string;
  }

  export interface QueueConsumer {
    id: string;
    appId: string;
    consumerName: string;
    consumerStatus: QueueConsumerStatus;
    createdAt: string;
    updatedAt: string | undefined;
    qualifiedJavaName: string;
    comment: string;
    routingKey: string;
  }
    
  export type QueueConsumerStatus = 'ENABLED' | 'DISABLED';
}
