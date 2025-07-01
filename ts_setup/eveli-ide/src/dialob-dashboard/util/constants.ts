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

export const dateOptions: Intl.DateTimeFormatOptions = {
	year: 'numeric',
	month: '2-digit',
	day: '2-digit',
	hour: 'numeric',
	minute: 'numeric',
	hour12: false,
};
