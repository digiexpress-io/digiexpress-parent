import React from 'react';
import { ThemeProvider, StyledEngineProvider } from '@mui/material/styles';

import { siteTheme, Frontdesk } from '@dxs-ts/eveli-ide';



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


export const FrontdeskApp: React.FC = () => {
  const locale = getLocale();

  return (
      <StyledEngineProvider injectFirst>
        <ThemeProvider theme={siteTheme}>
          <Frontdesk configUrl='/config' defaultLocale={locale} />
        </ThemeProvider>
    </StyledEngineProvider>
  );
}
