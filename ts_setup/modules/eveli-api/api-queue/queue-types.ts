
export namespace QueueApi {

}

export declare namespace QueueApi {
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

  export interface QueueMessage {
    id: string;
    routingKey: string;
    
    comment: string;
    createdBy: string;
  
    createdAt: string;
    expiresAt: string;
    startsAt: string;
  
    
    bodyId: string;
    bodyType: string;
    bodyValue: object;
    
    updatedAt: string | undefined;
    status: QueueMessageStatus;
  }
  
  export type QueueMessageStatus = 'RESOLVING_ROUTING' | 'ROUTING_COMPLETED'

  export interface Delivery {
    id: string;
    messageId: string;
    queueId: string;
    consumerId: string;
    status: DeliveryStatus;
    
    createdAt: string;
    startsAt: string;
    expiresAt: string;
    completedAt: string | undefined;
    attempts: DeliveryAttempt[];
  }

  export interface DeliveryAttempt {
    id: string;
    deliveryId: string;
    
    createdAt: string;
    consumerComment: string | undefined;
    consumerError: object | undefined;
    consumerStatus: string | undefined;
  }
  
  export type DeliveryStatus = 'OPEN' | 'COMPLETED';
}
