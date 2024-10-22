
export interface DialobFormMetadata {
  label?: string;
    creator?: string;
    tenantId?: string;
    savedBy?: string;
    languages?: string[];
    valid?: boolean;
    created?: string;
    lastSaved?: string;
    purpose?: string;
    [prop: string]: any;
};

export interface DialobForm {
  _id: string;
  name: string;
  metadata: DialobFormMetadata;
  data?: any;
};

export interface DialobFormEntry {
  id: string;
  metadata: DialobFormMetadata;
};

export const DEFAULT_FORM: Partial<DialobForm> = {
  name: '',
  data: {
    questionnaire : {
      id: 'questionnaire',
      type: 'questionnaire',
      items: []
    }
  },
  metadata: {
    label: '',
    languages: [
      'fi',
      'en'
    ]
  }
};

export interface DialobCreateFormCommand {
  purpose: string;
  title: string;
};

export interface DialobFormTag {
  formLabel: string;
  formName: string;
  tagFormId: string;
  tagName: string;
};

export interface DialobQuestionnaireMetadata {
  status: 'NEW' | 'OPEN' | 'COMPLETED';
  formId: string;
  formName?: string;
  opened?: string;
  completed?: string;
}

export interface DialobQuestionnaire {
  _id: string;
  metadata: DialobQuestionnaireMetadata;
};
