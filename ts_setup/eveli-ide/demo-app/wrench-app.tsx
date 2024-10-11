
import React from 'react';
import { ThemeProvider, StyledEngineProvider } from '@mui/material/styles';
import { IntlProvider } from 'react-intl'
import { WrenchComposer, WrenchClient, wrenchIntl, siteTheme } from '@dxs-ts/eveli-ide';


const init = {
  locale: 'en',
  url: "http://localhost:8081/assets", //spring-app
};

console.log("INIT", init);

const store = new WrenchClient.StoreImpl(init);

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

export const WrenchApp: React.FC = () => {

  const locale = getLocale();
  const service = React.useMemo(() => new WrenchClient.ServiceImpl(store), [store]);
  
  return (
    <IntlProvider locale={locale} messages={wrenchIntl[locale]}>
      <StyledEngineProvider injectFirst>
        <ThemeProvider theme={siteTheme}>
          <WrenchComposer service={service} />
        </ThemeProvider>
      </StyledEngineProvider>
    </IntlProvider>
  );
}
