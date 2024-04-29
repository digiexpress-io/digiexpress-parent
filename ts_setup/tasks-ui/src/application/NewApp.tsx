import React from 'react';

import { IntlProvider } from 'react-intl';
import { ThemeProvider, StyledEngineProvider } from '@mui/material';
import { SnackbarProvider } from 'notistack';
import { siteTheme } from 'components-burger';
import { BackendProvider } from 'descriptor-backend';
import AppFrontoffice from 'app-frontoffice';


import { initLogging } from './LoggetSetup';
import messages from './intl';

import { initBackend, useHealth, useProfile } from './initBackend';
import { Loader } from './Loader';


const locale = 'en';
const backend = initBackend();

initLogging();


const InternalInit: React.FC<{}> = ({ }) => {
  const { health } = useHealth();
  const { profile } = useProfile();

  return (
    <>
      <Loader health={health} profile={profile}/>
      {health && profile && <AppFrontoffice profile={profile} /> }
    </>
  )
}


const NewApp: React.FC<{}> = () => (
  <IntlProvider locale={locale} messages={messages[locale]}>
    <StyledEngineProvider injectFirst>
      <ThemeProvider theme={siteTheme}>
        <SnackbarProvider>
          <BackendProvider backend={backend}>
            <InternalInit />
          </BackendProvider>
        </SnackbarProvider>
      </ThemeProvider>
    </StyledEngineProvider>
  </IntlProvider>);

export default NewApp;
