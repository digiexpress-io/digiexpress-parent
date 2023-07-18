import React from 'react';

import { IntlProvider } from 'react-intl';
import { ThemeProvider, StyledEngineProvider } from '@mui/material/styles';
import Burger, { siteTheme } from '@the-wrench-io/react-burger';
import Client, { messages, Main, Secondary, Toolbar, Composer } from '@the-wrench-io/hdes-ide';
import SnakbarWrapper from './SnakbarWrapper';

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

interface Csrf {
  key: string, value: string
}


const getUrl = () => {
  if (window._env_ && window._env_.url) {
    const url = window._env_.url;
    if (url.endsWith("/")) {
      return url.substring(0, url.length - 1)
    }
    return url;
  }

  return "http://localhost:8081/assets";
}

console.log("WINDOW CONFIG", window._env_);

const init = {
  locale: 'en',
  url: getUrl(),
  csrf: window._env_?.csrf,
  oidc: window._env_?.oidc,
  status: window._env_?.status,
};

console.log("INIT", init);


const store: Client.Store = new Client.StoreImpl(init);


const CreateApps: React.FC<{}> = () => {
  // eslint-disable-next-line 
  const backend = React.useMemo(() => new Client.ServiceImpl(store), [store]);
  const wrenchComposer: Burger.App<Composer.ContextType> = {
    id: "wrench-composer",
    components: { primary: Main, secondary: Secondary, toolbar: Toolbar },
    state: [
      (children: React.ReactNode, restorePoint?: Burger.AppState<Composer.ContextType>) => (<>{children}</>),
      () => ({})
    ]
  };

  return (
    <Composer.Provider service={backend}>
      <Burger.Provider children={[wrenchComposer]} secondary="toolbar.assets" drawerOpen/>
    </Composer.Provider>
  )
}

const NewApp = (
  <IntlProvider locale={init.locale} messages={messages[init.locale]}>
    <StyledEngineProvider injectFirst>
      <ThemeProvider theme={siteTheme}>
        <SnakbarWrapper>
          <CreateApps />
        </SnakbarWrapper>
      </ThemeProvider>
    </StyledEngineProvider>
  </IntlProvider>);

export default NewApp;









