export type LocalizedString = {
  [language: string]: string;
};

export type Variable = {
  name: string;
  expression: string;
};

export type ContextVariableType = 'text' | 'number' | 'decimal' | 'boolean' | 'date' | 'time';

export type ContextVariable = {
  name: string;
  defaultValue ?: any;
  context: boolean;
  contextType: string; // TODO: ContextVariableType
};

export const isContextVariable = (variable: ContextVariable | Variable): variable is ContextVariable => (variable as ContextVariable).context === true;

export type ValueSetEntry = {
  id: string;
  label: LocalizedString;
  when?: string;
  [prop: string]: any;
}

export type ValueSet = {
  id: string;
  entries: ValueSetEntry[];
};

export type DialobItemType = 'questionnaire' | 'group' | 'text' | 'number' | 'boolean' | 'multichoice' | 'survey' | 'surveygroup' | 'list' | 'note' | 'date' | 'time' | 'decimal' | 'row' | 'rowgroup';

export type ValidationRule = {
  message?: LocalizedString;
  rule?: string;
};

export type DialobItemTemplate = {
  type: string; // TODO: DialobItemType
  view?: string;
  label?: LocalizedString;
  description?: LocalizedString;
  required?: string;
  valueSetId?: string;
  activeWhen?: string;
  items?: string[];
  className?: string[];
  props?: {
    [prop: string]: any;
  };
  validations?: ValidationRule[];
  [prop: string]: any;
};

export type DialobItem = DialobItemTemplate & {
  id: string;
}

export type ComposerState = {
  _id: string;
  _rev: string;
  name: string;
  data: {
    [item: string]: DialobItem;
  };
  variables ?: (Variable | ContextVariable)[];
  valueSets ?: ValueSet[];
  metadata: {
    label?: string;
    creator?: string;
    tenantId?: string;
    savedBy?: string;
    languages?: string[];
    valid?: boolean;
    created?: string;
    lastSaved?: string;
    composer?: {
      globalValueSets?: {
        label?: string;
        valueSetId: string;
      }[];
    },
    [prop: string]: any;
  }
};

export type ComposerCallbacks = {
  onAddItem?: (state: ComposerState, item: DialobItem) => void;
}