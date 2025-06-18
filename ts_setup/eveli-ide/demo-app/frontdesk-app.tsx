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
  router,
  TenantConfigContextProvider
} from '@dxs-ts/eveli-ide';

import { userTheme } from './theme';

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


function fetchOverrideForAttachments(parentFetch: typeof window.fetch): typeof window.fetch {
  return async (input: RequestInfo | URL, init?: RequestInit) => {
    // my custom fetch, override init
    alert("Attachments not supported");

    return parentFetch(input, init)
      .then(response => {

        return response;
      });
  }
}

function globalFetchOverride(): typeof window.fetch {

  return async (input: RequestInfo | URL, init?: RequestInit) => {

    const override: RequestInit = {
      ...(init ?? {}),
      headers: {
        ...(init?.headers ?? {}),
        'My-Header': 'Header-1'
      }
    }

    console.groupCollapsed('global fetch');
    console.log(input);
    console.log('original', init);
    console.log('override', override);
    console.groupEnd();

    return window.fetch(input, override)
  }
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

              <FetchProvider 
                tree={fetchtree.withFetch(globalFetchOverride)} 
                initContextPath='/'
                overrides={{
                  'worker/rest/api/tasks/$taskId/files.POST': fetchOverrideForAttachments
                }}>
                <ConfigContextProvider logoutUrl={logoutUrl} loginUrl={loginUrl}>
                  <TenantConfigContextProvider features={[
                    'visual_accommodation',
                    'stencil_locale_filter',
                    'eveli_publication_only',
                    'smart_tables',
                    'user_profile',
                    'batches']}>

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
