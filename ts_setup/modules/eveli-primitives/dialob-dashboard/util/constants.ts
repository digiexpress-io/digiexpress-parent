import type { FormConfigurationFilters, DefaultForm } from '../types';

export const DEFAULT_CONFIGURATION_FILTERS: FormConfigurationFilters = {
  label: undefined,
  latestTagDate: undefined,
  lastSaved: undefined,
  latestTagName: undefined,
  labels: undefined
};

export const DEFAULT_FORM: DefaultForm = {
  name: '',
  data: {
    questionnaire: {
      id: 'questionnaire',
      type: 'questionnaire',
      items: []
    }
  },
  metadata: {
    label: '',
    languages: ['fi', 'en']
  }
};

export enum LabelAction {
  ADD = 'ADD',
  DELETE = 'DELETE'
}
