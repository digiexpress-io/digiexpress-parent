import React from 'react';
import { ThemeProvider, StyledEngineProvider } from '@mui/material/styles';
import { IntlProvider } from 'react-intl'
import { StencilComposer, StencilClient, stencilIntl, siteTheme } from '@dxs-ts/eveli-ide';

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


//const service = StencilClient.service({ config: { url: "http://localhost:8080/q/ide-services" }});

export const StencilApp: React.FC = () => {
  const locale = getLocale();
  const service = StencilClient.mock();

  return (
    <IntlProvider locale={locale} messages={stencilIntl[locale]}>
      <StyledEngineProvider injectFirst>
        <ThemeProvider theme={siteTheme}>
          <StencilComposer service={service} />
        </ThemeProvider>
      </StyledEngineProvider>
    </IntlProvider>
  );
}
