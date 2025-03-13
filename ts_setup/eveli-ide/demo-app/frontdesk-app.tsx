import React from 'react'
import { ThemeProvider, StyledEngineProvider } from '@mui/material/styles'
import { Button } from '@mui/material'

import { RouterProvider } from '@tanstack/react-router'
import { QueryClientProvider, QueryClient } from '@tanstack/react-query'
import { FormattedMessage } from 'react-intl';
import { SnackbarProvider } from 'notistack';


import {
  fetchtree, FetchProvider, LocaleProvider,
  IamBackendProvider, ConfigContextProvider,
  siteTheme, router,
} from '@dxs-ts/eveli-ide';


const queryClient = new QueryClient();

export const FrontdeskApp: React.FC = () => {
  const notistackRef = React.createRef<SnackbarProvider>();
  const handleCloseNotification = (key: string | number | undefined) => () => {
    notistackRef.current?.closeSnackbar(key);
  }

  async function handleExpire() {
    console.log("SESSION EXPIRED");
  }
  const logoutUrl = '/logout';
  const loginUrl = '/oauth2/authorization/oidcprovider';

  return (
    <QueryClientProvider client={queryClient}>
      <LocaleProvider>
        <SnackbarProvider maxSnack={3} ref={notistackRef}
          action={(key) => (<Button onClick={handleCloseNotification(key)}><FormattedMessage id='button.dismiss' /></Button>)}
          anchorOrigin={{ vertical: 'bottom', horizontal: 'left' }}
        >

          {/** config context will update context path to whatever /config.serviceUrl will return */}
          <FetchProvider tree={fetchtree} initContextPath='/'>
            <ConfigContextProvider logoutUrl={logoutUrl} loginUrl={loginUrl}>

              <StyledEngineProvider injectFirst>
                <ThemeProvider theme={siteTheme}>

                  <IamBackendProvider onExpire={handleExpire}>
                    <RouterProvider router={router} />
                  </IamBackendProvider>

                </ThemeProvider>
              </StyledEngineProvider>
              
            </ConfigContextProvider>
          </FetchProvider>

        </SnackbarProvider>
      </LocaleProvider>
    </QueryClientProvider>
  );
}
