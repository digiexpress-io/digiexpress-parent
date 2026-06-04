import { Fs } from "../fs-types";


export const ALL_TYPES: Fs.BodyType[] = [
  'ARTICLE',
  'ARTICLE_PAGE',
  'ARTICLE_LINK',
  'ARTICLE_WORKFLOW',
  'ARTICLE_TEMPLATE',
  'FLOW',
  'FLOW_TASK',
  'DECISION_TABLE',
  'LOCALE',
  'PRINTOUT',
  'PRINTOUT_PAGE',
  'PRINTOUT_RESOURCE',
  'FOLDER',
  'DIALOB_FORM',
];


const ALL_CONFIG_OPTIONS: Fs.SelectOption[] = [
  { value: 'DEV_MODE', label: 'Development mode' },
  { value: 'ASSIGNABLE_MODE', label: 'Assignable mode' },
  { value: 'DISABLED_MODE', label: 'Disabled mode' },
  { value: 'ANONYMOUS_MODE', label: 'Anonymous mode' },
  { value: 'AUTH_ONLY_MODE', label: 'Auth only mode' },
];

export function getExtension(type: Fs.BodyType): string | undefined {
  switch (type) {
    case 'FOLDER': return undefined;
    case 'ARTICLE_PAGE': return '.page';
    case 'ARTICLE_WORKFLOW': return '.workflow';
    case 'DECISION_TABLE': return '.dt';
    case 'DIALOB_FORM': return '.dialob';
    case 'ARTICLE_LINK': return '.link';
    case 'PRINTOUT': return '.printout';
    case 'PRINTOUT_RESOURCE': return '.resource';
    case 'PRINTOUT_PAGE': return '.printout-page';
    default: return '.' + type.toLowerCase().replaceAll('_', '');
  }
}

export function getConfigOptionsForType(type: Fs.BodyType): Fs.SelectOption[] {
  switch (type) {
    case 'ARTICLE_LINK': {
      return ALL_CONFIG_OPTIONS.filter(o => o.value === 'DEV_MODE' || o.value === 'DISABLED_MODE');
    }
    case 'ARTICLE_WORKFLOW': {
      return ALL_CONFIG_OPTIONS
    }
    case 'ARTICLE': {
      return ALL_CONFIG_OPTIONS.filter(o => o.value === 'DEV_MODE' || o.value === 'AUTH_ONLY_MODE');
    }
    case 'LOCALE': {
      return ALL_CONFIG_OPTIONS.filter(o => o.value === 'DISABLED_MODE');
    }
    case 'ARTICLE_PAGE': {
      return ALL_CONFIG_OPTIONS.filter(o => o.value === 'DEV_MODE' || o.value === 'DISABLED_MODE');
    }
    case 'PRINTOUT': {
      return ALL_CONFIG_OPTIONS.filter(o => o.value === 'DEV_MODE');
    }
    case 'FLOW': {
      return ALL_CONFIG_OPTIONS.filter(o => o.value === 'DEV_MODE' || o.value === 'DISABLED_MODE')
    }
    default: {
      return [];
    }
  }
}
