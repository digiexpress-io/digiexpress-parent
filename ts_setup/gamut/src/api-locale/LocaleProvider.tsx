import React from 'react';
import { IntlProvider } from 'react-intl';
import { LocaleApi } from './locale-types';
import locales from '../intl';


export const LocaleContext = React.createContext<LocaleApi.LocaleContextType>({} as any);
export type LocalCode = string;


export interface LocaleProviderProps {
  children: React.ReactNode;
  options?: LocaleApi.Localizations;
  defaultLocale?: () => string;
  disableErrors?: boolean;
}

export const LocaleProvider: React.FC<LocaleProviderProps> = (props) => {
  const { options = {}, defaultLocale } = props;

  const messages: any = React.useMemo(() => merge(options), [options]);
  const [locale, setLocale] = React.useState<string>(getLocale(defaultLocale));
  const contextValue: LocaleApi.LocaleContextType = React.useMemo(() => Object.freeze({ locale, setLocale }), [locale]);
  const intlMessages = messages[locale];

  return (<LocaleContext.Provider value={contextValue}>
    <IntlProvider locale={locale} messages={intlMessages} onError={props.disableErrors ? ((err) => {}) : undefined}>
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

const getLocale = (defaultLocale: (() => string) | undefined) => {
  let selectedLocale = '';

  let nextIsLocale = false;
  for(const path of window.location.pathname.split('\/')) {
    if (path === 'secured' || path === 'public') {
      nextIsLocale = true
      continue;
    }
    if(nextIsLocale) {
      selectedLocale = path;
      break;
    }
  }

  let locale = 'en';
  if (selectedLocale) {
    locale = selectedLocale;
  }
  else {
    const language = navigator.language;
    if (language.length > 2) {
      locale = language.split("-")[0];
    }
    else {
      locale = language;
    }
  }
  if (locale !== 'en' && locale !== 'fi') {
    return defaultLocale ? defaultLocale() : 'en';
  }
  return locale;
}

