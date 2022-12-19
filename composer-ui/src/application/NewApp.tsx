import React from 'react';

import { IntlProvider, useIntl } from 'react-intl';
import { ThemeProvider, StyledEngineProvider } from '@mui/material';
import { SnackbarProvider } from 'notistack';
import { useSnackbar } from 'notistack';
import Burger, { siteTheme } from '@the-wrench-io/react-burger';

import DeClient from '@declient';
import AppCore from '../core';

import Connection from './Connection';


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
  return "http://localhost:8081/q/digi/rest/api/";
}

const store: DeClient.Store = new DeClient.DefaultStore({
  url: getUrl(),
  csrf: window._env_?.csrf,
  oidc: window._env_?.oidc,
  status: window._env_?.status,
});
const backend = new DeClient.ServiceImpl(store);

const Apps: React.FC<{services: DeClient.HeadState}> = ({services}) => {
  // eslint-disable-next-line 
  const serviceComposer: Burger.App<DeClient.ComposerContextType> = React.useMemo(() => ({
    id: "service-composer",
    components: { primary: AppCore.Main, secondary: AppCore.Secondary, toolbar: AppCore.Toolbar },
    state: [
      (children: React.ReactNode, restorePoint?: Burger.AppState<DeClient.ComposerContextType>) => (<>{children}</>),
      () => ({})
    ]
  }), [AppCore]);

  return (<DeClient.Provider service={backend} head={services}>
    <Burger.Provider children={[serviceComposer]} secondary="toolbar.assets" drawerOpen />
  </DeClient.Provider>)
}

const LoadApps = React.lazy(async () => {
  const head = await backend.head();
  if(head.contentType === 'NO_CONNECTION') {
    const Result: React.FC<{}> = () => <Connection.Down client={backend} />;
    return ({default: Result})
  } else if (head.contentType === 'BACKEND_NOT_FOUND') {
    const Result: React.FC<{}> = () => <Connection.Misconfigured client={backend} />;
    return ({default: Result})    
  }
  const Result: React.FC<{}> = () => {
    const snackbar = useSnackbar(); 
    const intl = useIntl();
    React.useEffect(() => {
      if(head.contentType === 'OK') {
        const msg = intl.formatMessage({ id: 'init.loaded'}, {name: head.name});
        snackbar.enqueueSnackbar(msg, {variant: 'success'})
      }
    }, [head.name]);
    return <Apps services={head}/>
  };
  return ({default: Result}) 
});

const locale = 'en';
const NewApp = (
  <IntlProvider locale={locale} messages={AppCore.messages[locale]}>
    <StyledEngineProvider injectFirst>
      <ThemeProvider theme={siteTheme}>
        <SnackbarProvider>
          <React.Suspense fallback={<Connection.Loading client={backend} />}><LoadApps /></React.Suspense>
        </SnackbarProvider>
      </ThemeProvider>
    </StyledEngineProvider>
  </IntlProvider>);

export default NewApp;









