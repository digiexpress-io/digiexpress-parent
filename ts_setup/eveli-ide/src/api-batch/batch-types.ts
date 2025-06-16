
export namespace BatchApi {

}

export declare namespace BatchApi {

  export type BatchStatus = 'ENABLED' | 'DISABLED';
  export type RuntimeExecutionStatus = 'OK' | 'ERROR';
  export type RuntimeStatus = 'CREATED' | 'EXECUTING' | 'SKIPPED' | 'COMPLETED' | 'CANCELLED';


  export interface Batch {
    id: string;
    batchName: string; // unique id, technical name
    appId: string;
    externalId: string | undefined; 
    
    createdAt: string;
    createdBy: string;
    
    updatedAt: string | string;
    updatedBy: string | string;
    status: BatchStatus;
    
    comment: string;

    transitives?: {
        instances: RuntimeInstance[];
    }
  }

  export interface RuntimeInstance {
    id: string;
    batchId: string;
    
    createdAt: string;
    endedAt: string | string;
    
    name: string;
    status: RuntimeStatus;
    executionStatus: RuntimeExecutionStatus;
    
    comment: string;
  }
}