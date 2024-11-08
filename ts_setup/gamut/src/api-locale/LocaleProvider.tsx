import React from 'react';
import { IntlProvider } from 'react-intl';
import { LocaleApi } from './locale-types';


export const LocaleContext = React.createContext<LocaleApi.LocaleContextType>({} as any);
export type LocalCode = string;


export interface LocaleProviderProps {
  children: React.ReactNode;
  options?: LocaleApi.Localizations;
}

export const LocaleProvider: React.FC<LocaleProviderProps> = (props) => {
  const { options = {} } = props;

  const messages: any = React.useMemo(() => merge(options), [options]);
  const [locale, setLocale] = React.useState<string>(getLocale());
  const contextValue: LocaleApi.LocaleContextType = React.useMemo(() => Object.freeze({ locale, setLocale }), [locale]);
  const intlMessages = messages[locale];

  return (<LocaleContext.Provider value={contextValue}>
    <IntlProvider locale={locale} messages={intlMessages}>
      {props.children}
    </IntlProvider>
  </LocaleContext.Provider>);
}

export const useLocale = () => {
  return React.useContext(LocaleContext);
}

function merge(options: LocaleApi.Localizations): LocaleApi.Localizations {
  const { en = {} } = options;
  const { fi = {} } = options;
  const otherLocales = {...options};
  delete otherLocales['en'];
  delete otherLocales['fi'];

  return { 
    en: {...LocaleApi.en, ...en}, 
    fi: {...LocaleApi.fi, ...fi}, 
    ...otherLocales
  };
}

const getLocale = () => {
  let locale = 'en';

  // filter empty elements to avoid empty first element when pathname starts with '/'
  const [, , selectedLocale] = window.location.pathname.split('\/').filter(pathElement=>pathElement);
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
    return 'en';
  }
  return locale;
}

