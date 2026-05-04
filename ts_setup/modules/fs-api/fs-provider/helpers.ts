import { Fs } from "../fs-types";


export const ALL_TYPES: Fs.BodyType[] = [
  'FOLDER',
  'ARTICLE',
  'ARTICLE_WORKFLOW',
  'DIALOB_FORM',
  'FLOW',
  'LOCALE',
  'PRINTOUT',
  'ARTICLE_PAGE',
  'ARTICLE_TEMPLATE',
  'ARTICLE_LINK',
];


const ALL_CONFIG_OPTIONS: Fs.SelectOption[] = [
  { value: 'devMode', label: 'Development mode' },
  { value: 'assignableMode', label: 'Assignable mode' },
  { value: 'disabledMode', label: 'Disabled mode' },
  { value: 'anonymousMode', label: 'Anonymous mode' },
];

export function getExtension(type: Fs.BodyType): string | undefined {
  switch (type) {
    case 'FOLDER': return undefined;
    case 'ARTICLE_PAGE': return '.page';
    case 'ARTICLE_WORKFLOW': return '.workflow';
    case 'DECISION_TABLE': return '.dt';
    case 'DIALOB_FORM': return '.dialob';
    case 'ARTICLE_LINK': return '.link';
    default: return '.' + type.toLowerCase().replaceAll('_', '');
  }
}

export function getConfigOptionsForType(type: Fs.BodyType): Fs.SelectOption[] {
  switch (type) {
    case 'ARTICLE_LINK': {
      return ALL_CONFIG_OPTIONS.filter(o => o.value === 'devMode' || o.value === 'disabledMode');
    }
    case 'ARTICLE_WORKFLOW':
    case 'ARTICLE': {
      return ALL_CONFIG_OPTIONS;
    }
    case 'LOCALE': {
      return ALL_CONFIG_OPTIONS.filter(o => o.value === 'disabledMode');
    }
    case 'ARTICLE_PAGE': {
      return ALL_CONFIG_OPTIONS.filter(o => o.value === 'disabledMode' || o.value === 'devMode');
    }
    case 'PRINTOUT': {
      return ALL_CONFIG_OPTIONS.filter(o => o.value === 'devMode');
    }
    default: {
      return [];
    }
  }
}
