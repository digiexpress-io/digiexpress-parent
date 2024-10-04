import {
  en as enIntl
} from './en'

import {
  fi as fiIntl
} from './fi'

export namespace LocaleApi {
  export const en = enIntl;
  export const fi = fiIntl;
}

export declare namespace LocaleApi {
  export type TranslationKey = keyof (typeof enIntl) | keyof (typeof fiIntl );
  export type LocalCode = string;
  export type LocalizedValue = string;
  export type Localization = Partial<Record<TranslationKey, LocalizedValue>>;
  export type Localizations = Record<LocalCode, Localization | {}>;

  export interface LocaleContextType {
    locale: string
    setLocale: (newLocale: string) => void
  }
  
}