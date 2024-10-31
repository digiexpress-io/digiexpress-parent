export interface WorkflowReleaseEntry {
  name: string;
  formName: string;
  formTag: string;
  flowName: string;
  updated?: Date;
}

export interface WorkflowRelease {
  id: number;
  body: {

    description: string;

    name: string;
    flowName: string;
    created?: Date;
    createdBy?: string;
    updated?: Date;
    entries: WorkflowReleaseEntry[];
  }
}

