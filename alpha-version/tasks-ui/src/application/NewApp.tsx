import React from 'react';

import { IntlProvider } from 'react-intl';
import { ThemeProvider, StyledEngineProvider } from '@mui/material';
import { SnackbarProvider } from 'notistack';
import { siteTheme } from 'components-burger';
import { BackendProvider } from 'descriptor-backend';
import AppFrontoffice from 'app-frontoffice';


import { initLogging } from './LoggetSetup';
import messages from './intl';

import { initBackend } from './initBackend';
import { AvatarProvider } from 'descriptor-avatar';
import { AccessMgmtContextProvider } from 'descriptor-access-mgmt';
import { TasksProvider } from 'descriptor-task';
import { DialobProvider } from 'descriptor-dialob';
import { EventsProvider } from 'descriptor-events';


const locale = 'en';
const backend = initBackend();
initLogging();




const NewApp: React.FC<{}> = () => (
  <IntlProvider locale={locale} messages={messages[locale]}>
    <StyledEngineProvider injectFirst>
      <ThemeProvider theme={siteTheme}>
        <SnackbarProvider>
          <BackendProvider backend={backend}>
            <AvatarProvider>
              <AccessMgmtContextProvider>
                <TasksProvider>
                  <DialobProvider>
                    <EventsProvider>
                      <AppFrontoffice />
                    </EventsProvider>
                  </DialobProvider>
                </TasksProvider>
              </AccessMgmtContextProvider>
            </AvatarProvider>
          </BackendProvider>
        </SnackbarProvider>
      </ThemeProvider>
    </StyledEngineProvider>
  </IntlProvider>);

export default NewApp;
