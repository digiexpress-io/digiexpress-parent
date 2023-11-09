import React from 'react';

import { IntlProvider, useIntl } from 'react-intl';
import { ThemeProvider, StyledEngineProvider, Theme } from '@mui/material';
import { SnackbarProvider } from 'notistack';
import { useSnackbar } from 'notistack';
import Burger, { siteTheme } from 'components-burger';

import TaskClient from 'client';
import Context from 'context';
import { ProjectIdProvider } from 'descriptor-project';
import Connection from './Connection';
import messages from './intl';
import Provider from './Provider';
import AppTasks from 'app-tasks';
import AppTenant from 'app-tenant';
import AppProjects from 'app-projects';
import AppStencil from 'app-stencil';
import AppFrontoffice from 'app-frontoffice';

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
  return "http://localhost:8080/q/digiexpress/api/";
}

const store: TaskClient.Store = new TaskClient.DefaultStore({
  urls: [
    { id: 'generic', url: getUrl() },
    { id: 'dialob', url: "http://localhost:92/dialob/" },
  ],
  performInitCheck: false,
  csrf: window._env_?.csrf,
  oidc: window._env_?.oidc,
  status: window._env_?.status,
});

const backend = new TaskClient.ServiceImpl(store)


const Apps: React.FC<{ profile: TaskClient.Profile }> = ({ profile }) => {
  const { projectId } = Context.useProjectId();
  const service = React.useMemo(() => {
    return backend.withProjectId(projectId);
  }, [projectId]);

  const stencil: Burger.App<{}, any> = React.useMemo(() => AppStencil(service, profile, projectId), [service, profile, projectId]);
  const tasks: Burger.App<{}, any> = React.useMemo(() => AppTasks(service, profile), [service, profile]);
  const projects: Burger.App<{}, any> = React.useMemo(() => AppProjects(service, profile), [service, profile]);
  const frontoffice: Burger.App<{}, any> = React.useMemo(() => AppFrontoffice(service, profile), [service, profile]);
  const tenant: Burger.App<{}, any> = React.useMemo(() => AppTenant(service, profile), [service, profile]);
  const appId = 'app-tenant';

  return (<Provider service={service} profile={profile}>
    <Burger.Provider children={
      [
        tenant, tasks, projects, stencil, frontoffice,
      ]
    } secondary="toolbar.activities" drawerOpen appId={appId} />
  </Provider>)
}


const CheckAppConnection = React.lazy(async () => {
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
    return <ProjectIdProvider projectId=""><Apps profile={head} /></ProjectIdProvider>
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

const NewApp: React.FC<{}> = () => (
  <IntlProvider locale={locale} messages={messages[locale]}>
    <StyledEngineProvider injectFirst>
      <ThemeProvider theme={theme}>
        <SnackbarProvider>
          <React.Suspense fallback={<Connection.Loading client={backend} />}><CheckAppConnection /></React.Suspense>
        </SnackbarProvider>
      </ThemeProvider>
    </StyledEngineProvider>
  </IntlProvider>);

export default NewApp;
