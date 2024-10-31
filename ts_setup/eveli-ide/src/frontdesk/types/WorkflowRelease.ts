import { Workflow } from "./Workflow";

export interface WorkflowReleaseEntry extends Workflow {

}

export interface WorkflowRelease {
  id: number;
  description: string;
  entries: WorkflowReleaseEntry[];

  name: string;
  flowName: string;
  created?: Date;
  createdBy?: string;
  updated?: Date;
}

