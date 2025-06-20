
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

    transitives?: {
      steps: RuntimeStep[];
      metrics: RuntimeMetric[];
    }
  }

  export interface RuntimeStep {
    id: string;
    runtimeId: string;
    consumerId: string;
    
    status: RuntimeStatus;
    executionStatus: RuntimeExecutionStatus;
    
    createdAt: string;
    endedAt: string | undefined;
    
    name: string;
    comment: string;
    transitives?: {
      metrics: RuntimeMetric[];
      stepRows: RuntimeStepRow[];
    }
  }

  export interface RuntimeStepRow {
    id: string;
    runtimeId: string;
    stepId: string;

    executionStatus: RuntimeExecutionStatus;
    
    createdAt: string;
    endedAt: string | undefined;
    
    rowNumber: number;
    externalId: string;
  
    input: object | undefined;
    output: any | undefined;

    comment: string;
  }

  export interface RuntimeMetric {
    id: string;
    runtimeId: string;
    stepId: string | undefined;
    
    name: 'batch-metrics';
    createdAt: string;
    updatedAt: string | undefined;
    valueStructured: {
      map: {
        cheapId: string | undefined;
        maxCost: number | undefined;
        minCost: number | undefined;
        stepName: string | undefined;
        failCount: number | undefined;
        expensiveId: string | undefined;
        successCount: number | undefined;
      }
    } | undefined;
 
  }
}