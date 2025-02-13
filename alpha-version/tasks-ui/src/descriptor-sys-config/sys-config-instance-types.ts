type Instant = string;

export interface SysConfigInstance {
  id: string;
  tenantId: string;
  ownerId: string;
  steps: Step<any>[];
}  


export interface Step<T extends StepBody> {
  id: string;
  targetDate: Instant;
  errors: SysConfigError[];
  body: T;
}


export interface StepBody {
  type: StepType;
}


export interface ProcessCreated extends StepBody {
  id: string;
  serviceName: string;
  formId: string;
  flowName: string;
  
  serviceId: string;
  releaseId: string;
  releaseName: string;
  
  params: Record<string, any>;
  
  type: 'PROCESS_CREATED';
}

export interface FillCreated extends StepBody {
  questionnaireSessionId: string;
  type: 'FILL_CREATED';
}

export interface FillCompleted extends StepBody {
  questionnaireSessionId: string;
  type: 'FILL_COMPLETED';
}


export interface FlowCompleted extends StepBody {
  accepts: Record<string, any>;
  returns: Record<string, any>;
  type: 'FLOW_COMPLETED'
}

export interface ProcessCompleted extends StepBody { }
  
export interface SysConfigError {
  id: string;
  msg: string;
}
  
export type StepType = 'PROCESS_CREATED' | 'FILL_CREATED' | 'FILL_COMPLETED' | 'FLOW_COMPLETED' | 'PROCESS_COMPLETED';