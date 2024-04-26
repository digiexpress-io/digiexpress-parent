import React from 'react';

import { IntlProvider, useIntl } from 'react-intl';
import { ThemeProvider, StyledEngineProvider } from '@mui/material';
import { SnackbarProvider } from 'notistack';
import { useSnackbar } from 'notistack';

import AppStencil from 'app-stencil';
import AppHdes from 'app-hdes';
import AppFrontoffice from 'app-frontoffice';
import LoggerFactory from 'logger';

import Burger, { siteTheme } from 'components-burger';
import Backend from 'descriptor-backend';
import { UserProfileAndOrg, ImmutableAmStore, TenantConfig, AccessMgmtContextProvider } from 'descriptor-access-mgmt';

import { BackendProvider } from 'descriptor-backend';
import { getLogProps } from './_log_props_';
import Connection from './Connection';
import messages from './intl';


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

const store: Backend.Store = new Backend.BackendStoreImpl({
  urls: {
    'TASKS': baseUrl + "/q/digiexpress/api/",
    'TENANT': baseUrl + "/q/digiexpress/api/",
    'CRM': baseUrl + "/q/digiexpress/api/",
    'STENCIL': baseUrl + "/q/digiexpress/api/",
    'USER_PROFILE': baseUrl + "/q/digiexpress/api/",
    'WRENCH': baseUrl + "/q/digiexpress/api/",
    'CONFIG': baseUrl + "/q/digiexpress/api/",
    'HEALTH': baseUrl + "/q/digiexpress/api/",
    'SYS_CONFIG': baseUrl + "/q/digiexpress/api/",
    'PERMISSIONS': baseUrl + "/q/digiexpress/api/",
    'DIALOB': baseUrl + "/q/digiexpress/api/",
    'AVATAR': baseUrl + "/q/digiexpress/api/",
  },
  performInitCheck: false,
  csrf: window._env_?.csrf,
  oidc: window._env_?.oidc,
  status: window._env_?.status,
});

const backend = new Backend.BackendImpl(store)

const TenantConfigSetup: React.FC<{ profile: UserProfileAndOrg, tenantConfig: TenantConfig }> = ({ profile, tenantConfig }) => {
  const hdes: Burger.App<{}, any> = React.useMemo(() => AppHdes(backend, profile, tenantConfig!), [backend, profile, tenantConfig]);
  const stencil: Burger.App<{}, any> = React.useMemo(() => AppStencil(backend, profile, tenantConfig!), [backend, profile, tenantConfig]);
  const frontoffice: Burger.App<{}, any> = React.useMemo(() => AppFrontoffice(backend, profile), [backend, profile]);
  const appId = tenantConfig.preferences.landingApp;
  return (<Burger.Provider secondary="toolbar.activities" drawerOpen appId={appId} children={[ stencil, hdes, frontoffice ]} />)
}



const CheckAppConnection = React.lazy(async () => {
  
  const head = await Promise.all([backend.health(), new ImmutableAmStore(backend.store).currentUserProfile()]);
  const [health, profile] = head;

  if (health.contentType === 'NO_CONNECTION') {
    const Result: React.FC<{}> = () => <Connection.Down client={backend} />;
    return ({ default: Result })
  } else if (health.contentType === 'BACKEND_NOT_FOUND') {
    const Result: React.FC<{}> = () => <Connection.Misconfigured client={backend} />;
    return ({ default: Result })
  }
  
  const tenantConfig = profile.tenant;
  const Result: React.FC<{}> = () => {
    const snackbar = useSnackbar();
    const intl = useIntl();
    
    React.useEffect(() => {
      if (health.contentType === 'OK') {
        const msg = intl.formatMessage({ id: 'init.loaded' }, { name: tenantConfig.name });
        snackbar.enqueueSnackbar(msg, { variant: 'success' })
      }
    }, [intl, snackbar]);
    return (
      <BackendProvider backend={backend}>
        <AccessMgmtContextProvider profile={profile} tenantConfig={tenantConfig}>
          <TenantConfigSetup profile={profile} tenantConfig={tenantConfig}/>
        </AccessMgmtContextProvider>
      </BackendProvider>
    )
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
