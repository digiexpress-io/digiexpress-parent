import React from 'react'
import { ThemeProvider, StyledEngineProvider } from '@mui/material/styles'
import { Button } from '@mui/material'

import { RouterProvider } from '@tanstack/react-router'
import { QueryClientProvider, QueryClient } from '@tanstack/react-query'
import { FormattedMessage } from 'react-intl';
import { SnackbarProvider } from 'notistack';


import { 
  fetchtree, FetchProvider, LocaleProvider, IamBackendProvider,
  siteTheme, router, ConfigContextProvider 
 } from '@dxs-ts/eveli-ide';


const queryClient = new QueryClient();

export const FrontdeskApp: React.FC = () => {
  const notistackRef = React.createRef<SnackbarProvider>();
  const handleCloseNotification = (key: string | number | undefined) => () => {
    notistackRef.current?.closeSnackbar(key);
  }
  async function handleExpire() {
    alert("SESSION EXPIRED");
  }
  const logoutUrl = '/logout';
  const loginUrl = '/oauth2/authorization/oidcprovider';
  
  return (
    <QueryClientProvider client={queryClient}>
      <FetchProvider tree={fetchtree} contextPath='/'>
        <StyledEngineProvider injectFirst>
          <ThemeProvider theme={siteTheme}>
            <LocaleProvider>
              <SnackbarProvider maxSnack={3} ref={notistackRef}
                action={(key) => (<Button onClick={handleCloseNotification(key)}><FormattedMessage id='button.dismiss' /></Button>)}
                anchorOrigin={{ vertical: 'bottom', horizontal: 'left' }}
              >
                <ConfigContextProvider logoutUrl={logoutUrl} loginUrl={loginUrl}>
                  <IamBackendProvider onExpire={handleExpire}>
                    <RouterProvider router={router} />
                  </IamBackendProvider>
                </ConfigContextProvider>
              </SnackbarProvider>
            </LocaleProvider>
          </ThemeProvider>
        </StyledEngineProvider>
      </FetchProvider>
    </QueryClientProvider>
  );
}
