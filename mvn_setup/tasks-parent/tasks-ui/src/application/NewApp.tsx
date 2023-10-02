import React from 'react';

import { IntlProvider, useIntl } from 'react-intl';
import { ThemeProvider, StyledEngineProvider, Theme } from '@mui/material';
import { SnackbarProvider } from 'notistack';
import { useSnackbar } from 'notistack';
import Burger, { siteTheme } from '@the-wrench-io/react-burger';

import TaskClient from '@taskclient';
import Connection from './Connection';
import messages from './intl';
import Views from './Views';


interface Csrf { key: string, value: string }
declare global {
  interface Window {
    _env_: {
      url?: string,
      csrf?: Csrf,
      oidc?: string,
      status?: string,
    }
  }
}
const getUrl = () => {
  if (window._env_ && window._env_.url) {
    const url = window._env_.url;
    return url.endsWith("/") ? url.substring(0, url.length - 1) : url;
  }
  return "http://localhost:8080/q/tasks/api/";
}

const store: TaskClient.Store = new TaskClient.DefaultStore({
  url: getUrl(),
  csrf: window._env_?.csrf,
  oidc: window._env_?.oidc,
  status: window._env_?.status,
});
const backend = new TaskClient.ServiceImpl(store);

const Apps: React.FC<{ profile: TaskClient.Profile }> = ({ profile }) => {
  // eslint-disable-next-line 
  const serviceComposer: Burger.App<TaskClient.ComposerContextType> = React.useMemo(() => ({
    id: "service-composer",
    components: { primary: Views.Main, secondary: Views.Secondary, toolbar: Views.Toolbar },
    state: [
      (children: React.ReactNode, _restorePoint?: Burger.AppState<TaskClient.ComposerContextType>) => (<>{children}</>),
      () => ({})
    ]
  }), []);

  return (<TaskClient.Provider service={backend} profile={profile}>
    <Burger.Provider children={[serviceComposer]} secondary="toolbar.activities" drawerOpen />
  </TaskClient.Provider>)
}

const LoadApps = React.lazy(async () => {
  const head = await backend.profile.getProfile();
  if (head.contentType === 'NO_CONNECTION') {
    const Result: React.FC<{}> = () => <Connection.Down client={backend} />;
    return ({ default: Result })
  } else if (head.contentType === 'BACKEND_NOT_FOUND') {
    const Result: React.FC<{}> = () => <Connection.Misconfigured client={backend} />;
    return ({ default: Result })
  }
  const Result: React.FC<{}> = () => {
    const snackbar = useSnackbar();
    const intl = useIntl();
    React.useEffect(() => {
      if (head.contentType === 'OK') {
        const msg = intl.formatMessage({ id: 'init.loaded' }, { name: head.name });
        snackbar.enqueueSnackbar(msg, { variant: 'success' })
      }
    }, [intl, snackbar]);
    return <Apps profile={head} />
  };
  return ({ default: Result })
});

const theme: Theme = {
  ...siteTheme,
  components: {
    MuiTypography: {
      styleOverrides: {
        body1: {
          fontSize: '10pt'
        },
        body2: {
          fontSize: '12pt'
        }
      }
    }
  }
};
const locale = 'en';
console.log("theme ", theme);



const NewApp: React.FC<{}> = () => (
  <IntlProvider locale={locale} messages={messages[locale]}>
    <StyledEngineProvider injectFirst>
      <ThemeProvider theme={theme}>
        <SnackbarProvider>
          <React.Suspense fallback={<Connection.Loading client={backend} />}><LoadApps /></React.Suspense>
        </SnackbarProvider>
      </ThemeProvider>
    </StyledEngineProvider>
  </IntlProvider>);

export default NewApp;
