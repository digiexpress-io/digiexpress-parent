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
  EveliComponents, 
  router
} from '@dxs-ts/eveli-ide';

import { userTheme } from './theme';
import { TenantConfigContextProvider } from '@/api-tenant-config'

const queryClient = new QueryClient();


// Register the router instance for type safety
declare module '@tanstack/react-router' {
  interface Register {
    router: typeof router
  }
}

// Register eveli components
declare module '@mui/material' {
  export interface Components<Theme = unknown> extends EveliComponents<Theme> { }
}


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
      <StyledEngineProvider injectFirst>
        <ThemeProvider theme={userTheme}>

          <SnackbarProvider
            maxSnack={3}
            ref={notistackRef}
            action={(key) => (
              <Button onClick={handleCloseNotification(key)}>
                <FormattedMessage id='button.dismiss' />
              </Button>
            )}
            anchorOrigin={{ vertical: 'bottom', horizontal: 'left' }}
          >

            <FetchProvider tree={fetchtree} initContextPath='/'>
              <ConfigContextProvider logoutUrl={logoutUrl} loginUrl={loginUrl}>
                <TenantConfigContextProvider>
                  <IamBackendProvider onExpire={handleExpire}>
                    <RouterProvider router={router} />
                  </IamBackendProvider>
                </TenantConfigContextProvider>
              </ConfigContextProvider>
            </FetchProvider>

          </SnackbarProvider>

        </ThemeProvider>
      </StyledEngineProvider>
    </LocaleProvider>
  </QueryClientProvider>
);

}
