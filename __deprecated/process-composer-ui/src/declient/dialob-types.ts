import { DialobDataType } from './dialob-data-types';

export interface DialobTree {
  forms: Record<string, DialobFormDocument>;
  revs: Record<string, DialobFormRevisionDocument>;
  releases: Record<string, DialobFormReleaseDocument>
}

export interface DialobFormReleaseDocument {
  
}
export interface DialobFormDocument {
  id: string;
  data: {
    name: string;
    variables: DialobVariable[];
    data: Record<string | 'questionnaire', DialobDataType>
  }
}

export interface DialobVariable {
  name: string;
  context: boolean;
  contextType: string;
}

export interface DialobFormRevisionEntryDocument {
  id: string;
  formId: string;
  revisionName: string;
}

export interface DialobFormRevisionDocument {
  id: string;
  head: string;
  name: string;
  version: string;
  updated: string;
  entries: DialobFormRevisionEntryDocument[]
}