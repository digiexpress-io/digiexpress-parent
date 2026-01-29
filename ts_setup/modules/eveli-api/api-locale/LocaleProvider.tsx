import React from 'react';
import { IntlProvider } from 'react-intl';

import { LocaleApi } from './locale-types';
import { messages as locales } from '@dxs-ts/eveli-intl';



export const LocaleContext = React.createContext<LocaleApi.LocaleContextType>({} as any);
export type LocalCode = string;


export interface LocaleProviderProps {
  children: React.ReactNode;
  options?: LocaleApi.Localizations;

  defaultLocale?: () => string;

  // override locale for dates, inputLang => actualLang, by default always 'FI'
  defaultDateLocale?: (selectedLocale: string) => string;
}

export const LocaleProvider: React.FC<LocaleProviderProps> = (props) => {
  const { options = {}, defaultDateLocale = () => 'fi' } = props;

  const messages: any = React.useMemo(() => merge(options), [options]);
  const [locale, setLocale] = React.useState<string>(getLocale(props));

  const contextValue: LocaleApi.LocaleContextType = React.useMemo(() => {

    // delegate class 
    class ContextImpl implements LocaleApi.LocaleContextType {  
      get localeForDate() { return defaultDateLocale(locale); }
      get locale() { return locale; }
      get messages() { return messages; }
      setLocale(newLocale: string) { 
        setLocale(newLocale);
        EveliLocaleStore.save(newLocale);
      }
    }

    return new ContextImpl();
  }, [locale, messages]);

  const intlMessages = messages[locale];

  return (<LocaleContext.Provider value={contextValue}>
    <IntlProvider locale={locale} messages={intlMessages} onError={() => {}}>
      {props.children}
    </IntlProvider>
  </LocaleContext.Provider>);
}

export const useLocale = () => {
  return React.useContext(LocaleContext);
}

function merge(options: LocaleApi.Localizations): LocaleApi.Localizations {
  const merged: LocaleApi.Localizations = {};

  for (const [code, value] of Object.entries(locales)) {
    const overrides = options[code] ?? {};
    merged[code] = { ...value, ...overrides };
  }
  for (const [code, value] of Object.entries(options)) {
    if (!merged[code]) {
      merged[code] = value;
    }
  }
  return merged;
}

const getLocale = (props: LocaleProviderProps) => {

  if(EveliLocaleStore.isEnabled()) {
    return EveliLocaleStore.get()!;
  }

  const resolvedDefault = props.defaultLocale ? props.defaultLocale() : undefined;
  if (resolvedDefault) {
    return resolvedDefault;
  }

  let selectedLocale = '';

  let nextIsLocale = false;
  for (const path of window.location.pathname.split('/')) {
    if (path === 'secured' || path === 'public') {
      nextIsLocale = true;
      continue;
    }
    if (nextIsLocale) {
      selectedLocale = path;
      break;
    }
  }

  let locale = 'en';
  if (selectedLocale) {
    locale = selectedLocale;
  } else {
    const language = navigator.language;
    if (language.length > 2) {
      locale = language.split('-')[0];
    } else {
      locale = language;
    }
  }

  const supportedLocales = ['en', 'fi', 'sv'];
  if (!supportedLocales.includes(locale)) {
    console.warn(`Unsupported locale '${locale}' — falling back to 'en'`);
    return 'en';
  }

  return locale;
};



const STORAGE_KEY = 'eveli_locale_store';

class EveliLocaleStore {
  
  static get(): string | null {
    const data = localStorage.getItem(STORAGE_KEY);
    if (!data) return null;
    try {
      return JSON.parse(data);
    } catch {
      return null;
    }
  }

  static save(cockpit: String | null | undefined): void {
    if(cockpit) {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(cockpit));
    } else {
      localStorage.removeItem(STORAGE_KEY);
    }
  }

  static clear(): void {
    localStorage.removeItem(STORAGE_KEY);
  }

  static isEnabled(): boolean {
    return this.get() !== null;
  }
}