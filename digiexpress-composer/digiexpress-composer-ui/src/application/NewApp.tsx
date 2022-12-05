import React from 'react';

import { IntlProvider } from 'react-intl';
import { ThemeProvider, StyledEngineProvider, CircularProgress, LinearProgress } from '@mui/material';
import SnackbarProvider from './SnakbarWrapper';
import Burger, { siteTheme } from '@the-wrench-io/react-burger';
import Client, { messages, Main, Secondary, Toolbar, Composer } from '../core';


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

const store: Client.Store = new Client.StoreImpl({
  url: getUrl(),
  csrf: window._env_?.csrf,
  oidc: window._env_?.oidc,
  status: window._env_?.status,
});
const backend = new Client.ServiceImpl(store);

const Apps: React.FC<{services: Client.Site}> = ({services}) => {
  // eslint-disable-next-line 
  const serviceComposer: Burger.App<Composer.ContextType> = React.useMemo(() => ({
    id: "service-composer",
    components: { primary: Main, secondary: Secondary, toolbar: Toolbar },
    state: [
      (children: React.ReactNode, restorePoint?: Burger.AppState<Composer.ContextType>) => (<>{children}</>),
      () => ({})
    ]
  }), [Main, Secondary, Toolbar]);

  return (<Composer.Provider service={backend} head={services}>
    <Burger.Provider children={[serviceComposer]} secondary="toolbar.assets" drawerOpen />
  </Composer.Provider>)
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
    const snackbar = Composer.useSnackbar(); 
    React.useEffect(() => {
      if(head.contentType === 'OK') {
        snackbar.enqueueSnackbar({id: 'init.loaded', values: {name: head.name}}, {variant: 'success'})
      }
    }, [head.name]);
    return <Apps services={head}/>
  };
  return ({default: Result}) 
});

const locale = 'en';
const NewApp = (
  <IntlProvider locale={locale} messages={messages[locale]}>
    <StyledEngineProvider injectFirst>
      <ThemeProvider theme={siteTheme}>
        <SnackbarProvider>
          <React.Suspense fallback={<Connection.Loading client={backend} />}><LoadApps /></React.Suspense>
        </SnackbarProvider>
      </ThemeProvider>
    </StyledEngineProvider>
  </IntlProvider>);

export default NewApp;









