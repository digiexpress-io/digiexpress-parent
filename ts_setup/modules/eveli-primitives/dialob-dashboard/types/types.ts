export interface FormConfiguration {
  id: string;
  metadata: Metadata;
  latestTagName?: string;
  latestTagDate?: Date;
}

export interface FormConfigurationFilters {
  label: string | undefined;
  latestTagDate: Date | undefined;
  lastSaved: Date | undefined;
  latestTagName: string | undefined;
  labels: string | undefined;
}

export interface FormConfigurationTag {
  latestTagName: string;
  latestTagDate: Date;
}

export interface Metadata {
  label?: string,
  created: Date,
  lastSaved: Date,
  tenantId: string,
  modified: string,
  modifiedBy: string,
  labels: string[]
}

export interface DefaultForm {
  name: string;
  data: FormData;
  metadata: FormMetadata;
}

export interface FormData {
  questionnaire: FormQuestionnaire
}

export interface FormQuestionnaire {
  id: string;
  type: string;
  items: any
}

export interface FormMetadata {
  label: string;
  languages: string[]
}

export interface FormTag {
  formName: string;
  name: string;
  created: Date;
  formId: string;
  description: string;
};