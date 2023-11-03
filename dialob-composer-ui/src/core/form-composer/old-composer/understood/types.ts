import { FlagNameValues } from 'semantic-ui-react';

export type Format = 'languageToKey' | 'keyToLanguage';

export interface KeyTranslations {
  [language: string]: string | undefined
}

export interface Translations {
  [key: string]: KeyTranslations;
};

export interface KeyMeta {
  description?: string;
  needsWork?: boolean;
}

export interface LangMeta {
  longName?: string;
  flag?: FlagNameValues;
}

export type Metadata = {
  key?: {
    [key: string]: KeyMeta;
  },
  language?: {
    [language: string]: LangMeta;
  }
};

export type Language = string;
export type Key = string;
