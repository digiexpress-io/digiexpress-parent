import React from 'react';
import { IntlProvider } from 'react-intl';
import { AdapterDateFns } from "@mui/x-date-pickers/AdapterDateFnsV3";
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';

import { LocaleApi } from './locale-types';
import locales from '../../intl';

import { Locale } from 'date-fns';
import { enUS } from 'date-fns/locale/en-US';
import { fi } from 'date-fns/locale/fi';
import { sv } from 'date-fns/locale/sv';

const DATE_LOCALE_MAP: {[key: string]: Locale} = {
  en: enUS,
  fi: fi,
  sv: sv
};


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
      <LocalizationProvider dateAdapter={AdapterDateFns} adapterLocale={DATE_LOCALE_MAP[locale]}>
        {props.children}
      </LocalizationProvider>
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

const getLocale = () => {
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
    return 'en';
  }
  return locale;
}

