
export interface DialobTree {
  forms: Record<string, DialobFormDocument>;
  revs: Record<string, DialobFormRevisionDocument>;
}
export interface DialobFormDocument {
  id: string;
  data: {
    name: string;
    variables: DialobVariable[];
  }
}
export interface DialobVariable {
  name: string;
  context: boolean;
  contextType: string;
}

export interface DialobFormRevisionDocument {

}