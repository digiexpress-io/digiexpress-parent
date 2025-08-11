
export namespace ProcExecutionApi {

}

export declare namespace ProcExecutionApi {
  export interface ProcessExecution {
    id: number;
    status: string;
    questionnaireId: string;
    taskId?: string;
    taskRef?: string;
    userId?: string;
    created: Date;
    workflow: Workflow;  
  }


  export interface Workflow {
    id: string;
    type: string;
    body: {
      name: string;
      formName: string;
      formTag: string;
      flowName: string;
      updated?: Date;
    }
  }
}