import { Workflow } from "./Workflow";

export interface Process {
  id: number;
  status: string;
  questionnaire: string;
  task?: string;
  userId?: string;
  created: Date;
  workflow: Workflow;  
}