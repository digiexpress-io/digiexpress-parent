export namespace LocaleApi {

}

export declare namespace LocaleApi {
  export type TranslationKey = string;
  export type LocalCode = string;
  export type LocalizedValue = string;
  export type Localization = Partial<Record<TranslationKey, LocalizedValue>>;
  export type Localizations = Record<LocalCode, Localization | {}>;

  export interface LocaleContextType {
    locale: string
    messages: Localizations;
    setLocale: (newLocale: string) => void

  }
  
}