import React from 'react';

import { useIntl } from 'react-intl';
import { useSnackbar } from 'notistack';


import Backend from 'descriptor-backend';
import { ImmutableAmStore } from 'descriptor-access-mgmt';

import { BackendProvider } from 'descriptor-backend';
import Connection from './Connection';
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
  try {
    window.LOG_FACTORY.getLogger().debug(`application mode: ${process.env.REACT_APP_LOCAL_DEV_MODE}`);
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
        <AppFrontoffice profile={profile} tenantConfig={tenantConfig}/>
      </BackendProvider>
    )
  };
  return ({ default: Result })
});



export const BackendSetup: React.FC<{ }> = ({ }) => {
  return (<React.Suspense fallback={<Connection.Loading client={backend} />}>
    <CheckAppConnection />
  </React.Suspense>);
}