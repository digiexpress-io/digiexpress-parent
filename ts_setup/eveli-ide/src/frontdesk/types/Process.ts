

export interface Process {
  id: number;
  status: string;
  questionnaire: string;
  task?: string;
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