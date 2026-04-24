import { Fs } from "../fs-types";



const PROPS_MAP_KEYS: Record<Fs.Type, true> = {
  folder: true, article: true, service: true, dialob: true,
  flow: true, language: true, printout: true, image: true,
  page: true, template: true, link: true, phone: true,
};

export const ALL_TYPES = Object.keys(PROPS_MAP_KEYS) as unknown as Fs.BodyType[];


const ALL_CONFIG_OPTIONS: Fs.SelectOption[] = [
  { value: 'devMode', label: 'Development mode' },
  { value: 'assignableMode', label: 'Assignable mode' },
  { value: 'disabledMode', label: 'Disabled mode' },
  { value: 'anonymousMode', label: 'Anonymous mode' },
];

export function getConfigOptionsForType(type: Fs.Type): Fs.SelectOption[] {
  switch (type) {
    case 'link':
    case 'phone': {
      return ALL_CONFIG_OPTIONS.filter(o => o.value === 'devMode' || o.value === 'disabledMode');
    }
    case 'service':
    case 'article': {
      return ALL_CONFIG_OPTIONS;
    }
    case 'language': {
      return ALL_CONFIG_OPTIONS.filter(o => o.value === 'disabledMode');
    }
    case 'page': {
      return ALL_CONFIG_OPTIONS.filter(o => o.value === 'disabledMode' || o.value === 'devMode');
    }
    case 'printout': {
      return ALL_CONFIG_OPTIONS.filter(o => o.value === 'devMode');
    }
    default: {
      return [];
    }
  }
}