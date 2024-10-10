import React from 'react';
import ReactDOM from 'react-dom';
import { ThemeProvider, StyledEngineProvider } from '@mui/material/styles';
import { IntlProvider } from 'react-intl'
import { siteTheme } from '@the-wrench-io/react-burger';

import { StencilComposer, StencilClient, messages } from './core';

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

const locale = getLocale();
console.log("used locale", locale);

const service = StencilClient.mock();
//const service = StencilClient.service({ config: { url: "http://localhost:8080/q/ide-services" }});


ReactDOM.render(
  <React.StrictMode>
    <IntlProvider locale={locale} messages={messages[locale]}>
      <StyledEngineProvider injectFirst>
        <ThemeProvider theme={siteTheme}>
          <StencilComposer service={service} />
        </ThemeProvider>
      </StyledEngineProvider>
    </IntlProvider>
  </React.StrictMode>,
  document.getElementById('root')
);
