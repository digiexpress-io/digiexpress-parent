import React from 'react';
import ReactDOM from 'react-dom';
import { IntlProvider } from 'react-intl';
import { ThemeProvider, StyledEngineProvider } from '@mui/material/styles';
import { SnackbarProvider } from 'notistack';
import Burger, { siteTheme } from 'import Burger from '@/burger'';
import Client, { messages, Main, Secondary, Toolbar, Composer } from './core';


const init = {
  locale: 'en',
  url: "http://localhost:8081/assets", //spring-app
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
        <SnackbarProvider>
          <CreateApps />
        </SnackbarProvider>
      </ThemeProvider>
    </StyledEngineProvider>
  </IntlProvider>);


ReactDOM.render(
  NewApp,
  document.getElementById('root')
);