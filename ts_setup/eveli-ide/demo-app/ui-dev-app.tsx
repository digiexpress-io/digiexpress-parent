
import React from 'react';
import { ThemeProvider, StyledEngineProvider } from '@mui/material/styles';
import { IntlProvider } from 'react-intl'
import { intl, siteTheme } from '@dxs-ts/eveli-ide';
import { UiDev } from '../src/uiDev';


const getLocale = () => {
  let locale = (navigator.languages && navigator.languages[0]) || navigator.language || (navigator as any).userLanguage || 'en-US';
  if (locale.length > 2) {
    locale = locale.substring(0, 2);
  }
  if (['en', 'sv', 'fi'].includes(locale)) {
    return locale;
  }
  return 'en';
}
// <UiDevApp />
export const UiDevApp: React.FC = () => {

  const locale = getLocale();

  return (
    <IntlProvider locale={locale} messages={(intl as any)[locale]}>
      <StyledEngineProvider injectFirst>
        <ThemeProvider theme={siteTheme}>
        <UiDev />
        </ThemeProvider>
      </StyledEngineProvider>
    </IntlProvider>
  );
}
