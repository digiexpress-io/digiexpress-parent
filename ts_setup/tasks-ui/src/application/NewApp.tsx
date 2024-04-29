import React from 'react';

import { IntlProvider } from 'react-intl';
import { ThemeProvider, StyledEngineProvider } from '@mui/material';
import { SnackbarProvider } from 'notistack';

import { initLogging } from './LoggetSetup';

import { siteTheme } from 'components-burger';
import messages from './intl';

import { BackendSetup } from './BackendSetup';

const locale = 'en';

initLogging();

const NewApp: React.FC<{}> = () => (
  <IntlProvider locale={locale} messages={messages[locale]}>
    <StyledEngineProvider injectFirst>
      <ThemeProvider theme={siteTheme}>
        <SnackbarProvider>
          <BackendSetup />
        </SnackbarProvider>
      </ThemeProvider>
    </StyledEngineProvider>
  </IntlProvider>);

export default NewApp;
