import { Workflow } from "./Workflow";

export interface WorkflowReleaseEntry extends Workflow {

}

export interface WorkflowRelease {
  id: number;
  name: string;
  description: string;
  flowName: string;
  created?: Date;
  createdBy?: string;
  updated?: Date;
  entries: WorkflowReleaseEntry[];
}