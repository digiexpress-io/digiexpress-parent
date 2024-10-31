
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