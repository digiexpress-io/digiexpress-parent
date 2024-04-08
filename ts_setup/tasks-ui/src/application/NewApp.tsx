import React from 'react';

import { IntlProvider, useIntl } from 'react-intl';
import { ThemeProvider, StyledEngineProvider } from '@mui/material';
import { SnackbarProvider } from 'notistack';
import { useSnackbar } from 'notistack';
import Burger, { siteTheme } from 'components-burger';

import TaskClient, { TenantConfig } from 'client';
import Context from 'context';
import { TenantConfigProvider } from 'descriptor-tenant-config';
import { UserProfileAndOrg } from 'descriptor-user-profile';

import AppTenant from 'app-tenant';
import AppStencil from 'app-stencil';
import AppHdes from 'app-hdes';
import AppFrontoffice from 'app-frontoffice';
import LoggerFactory from 'logger';

import { getLogProps } from './_log_props_';
import Connection from './Connection';
import messages from './intl';
import Provider from './Provider';


window.LOGGER = {
  config: {
    format: 'STRING',
    level: 'ERROR',
    values: process.env.REACT_APP_LOCAL_DEV_MODE ? getLogProps() : {}
  }
}


const log = LoggerFactory.getLogger();


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
  try {
    log.debug(`application mode: ${process.env.REACT_APP_LOCAL_DEV_MODE}`);
    if (process.env.REACT_APP_LOCAL_DEV_MODE) {
      return "http://localhost:8080";
    }
    return "";
  } catch (error) {
    return "";
  }
}

const baseUrl = getUrl();

const store: TaskClient.Store = new TaskClient.DefaultStore({
  urls: [
    { id: 'TASKS', url: baseUrl + "/q/digiexpress/api/" },
    { id: 'TENANT', url: baseUrl + "/q/digiexpress/api/" },
    { id: 'CRM', url: baseUrl + "/q/digiexpress/api/" },
    { id: 'STENCIL', url: baseUrl + "/q/digiexpress/api/" },
    { id: 'USER_PROFILE', url: baseUrl + "/q/digiexpress/api/" },
    { id: 'WRENCH', url: baseUrl + "/q/digiexpress/api/" },
    { id: 'CONFIG', url: baseUrl + "/q/digiexpress/api/" },
    { id: 'HEALTH', url: baseUrl + "/q/digiexpress/api/" },
    { id: 'SYS_CONFIG', url: baseUrl + "/q/digiexpress/api/" },
    { id: 'PERMISSIONS', url: baseUrl + "/q/digiexpress/api/" },
    { id: 'EXT_DIALOB', url: baseUrl + "/q/digiexpress/api/dialob/" },
    { id: 'EXT_DIALOB_EDIT', url: baseUrl + "/q/digiexpress/api/dialob/api/edit" },

    /*
    { id: 'EXT_DIALOB', url: "http://localhost:92/dialob/" },
    { id: 'EXT_DIALOB_EDIT', url: "http://localhost:92/dialob/api" },
    */
  ],
  performInitCheck: false,
  csrf: window._env_?.csrf,
  oidc: window._env_?.oidc,
  status: window._env_?.status,
});

const backend = new TaskClient.ServiceImpl(store)


const TenantConfigSetup: React.FC<{ profile: UserProfileAndOrg }> = ({ profile }) => {
  const { tenantConfig } = Context.useTenantConfig();
  if (!tenantConfig) {
    throw new Error("Tenant must be defined!");
  }

  const service = React.useMemo(() => {
    return backend.withTenantConfig(tenantConfig!);
  }, [tenantConfig]);

  const hdes: Burger.App<{}, any> = React.useMemo(() => AppHdes(service, profile, tenantConfig!), [service, profile, tenantConfig]);
  const stencil: Burger.App<{}, any> = React.useMemo(() => AppStencil(service, profile, tenantConfig!), [service, profile, tenantConfig]);
  const dialob: Burger.App<{}, any> = React.useMemo(() => AppTenant(service, profile), [service, profile]);
  const frontoffice: Burger.App<{}, any> = React.useMemo(() => AppFrontoffice(service, profile), [service, profile]);

  const appId = tenantConfig.preferences.landingApp;

  return (<Provider service={service} profile={profile}>
    <Burger.Provider children={
      [
        stencil, hdes, dialob, frontoffice
      ]
    } secondary="toolbar.activities" drawerOpen appId={appId} />
  </Provider>)
}

const CheckAppConnection = React.lazy(async () => {
  const head = await backend.health();

  if (head.contentType === 'NO_CONNECTION') {
    const Result: React.FC<{}> = () => <Connection.Down client={backend} />;
    return ({ default: Result })
  } else if (head.contentType === 'BACKEND_NOT_FOUND') {
    const Result: React.FC<{}> = () => <Connection.Misconfigured client={backend} />;
    return ({ default: Result })
  }

  const profile = await backend.currentUserProfile();
  const tenantConfig = profile.tenant;

  const Result: React.FC<{}> = () => {
    const snackbar = useSnackbar();
    const intl = useIntl();
    React.useEffect(() => {
      if (head.contentType === 'OK') {
        const msg = intl.formatMessage({ id: 'init.loaded' }, { name: tenantConfig.name });
        snackbar.enqueueSnackbar(msg, { variant: 'success' })
      }
    }, [intl, snackbar]);
    return (<TenantConfigProvider tenantConfig={tenantConfig}><TenantConfigSetup profile={profile} /></TenantConfigProvider>)
  };
  return ({ default: Result })
});

const locale = 'en';


const NewApp: React.FC<{}> = () => (
  <IntlProvider locale={locale} messages={messages[locale]}>
    <StyledEngineProvider injectFirst>
      <ThemeProvider theme={siteTheme}>
        <SnackbarProvider>
          <React.Suspense fallback={<Connection.Loading client={backend} />}><CheckAppConnection /></React.Suspense>
        </SnackbarProvider>
      </ThemeProvider>
    </StyledEngineProvider>
  </IntlProvider>);

export default NewApp;
