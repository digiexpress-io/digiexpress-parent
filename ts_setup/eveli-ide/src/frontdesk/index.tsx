import React, { useState } from 'react';
import { Button } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import * as Burger from '@/burger';
import { BurgerApi } from '@/burger';

import { UserContextProvider } from './context/UserContext';
import { ConfigContextProvider } from './context/ConfigContext';
import { IAPSessionRefreshContext } from './context/SessionRefreshContext';

import { DATE_LOCALE_MAP } from './intl/datelocalization';
import { AdapterDateFns } from "@mui/x-date-pickers/AdapterDateFnsV3";
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';


import { TasksSetup } from './TasksSetup';
import { SnackbarProvider } from 'notistack';
import { FeedbackProvider } from './context/FeedbackContext';
import { Main } from './Main';

import { Composer } from 'stencil/context';
import { Secondary } from './Secondary';


export { frontdeskIntl } from './intl';


export interface FrontdeskProps {
  defaultLocale?: string | undefined;
  configUrl?: string | undefined;
}


const Toolbar: React.FC = () => {
  return <></>
}

export const Frontdesk: React.FC<FrontdeskProps> = (initProps) => {
  const { defaultLocale = 'en', configUrl = '/config' } = initProps;

  const [locale, setLocale] = useState<string>(defaultLocale);

  const notistackRef = React.createRef<SnackbarProvider>();
  const onClickDismiss = (key: string | number | undefined) => () => {
    notistackRef.current?.closeSnackbar(key);
  }


  const frontdeskApp: BurgerApi.App<Composer.ContextType> = {
    id: "frontdesk-app",
    components: { primary: Main, secondary: Secondary, toolbar: Toolbar },
    state: [
      (children: React.ReactNode, restorePoint?: BurgerApi.AppState<Composer.ContextType>) => (
        <>{children}</>),
      () => ({})
    ]
  };




  return (
    <ConfigContextProvider path={configUrl}>
      <IAPSessionRefreshContext>
        <FeedbackProvider>

          <SnackbarProvider maxSnack={3} ref={notistackRef}
            action={(key) => (
              <Button onClick={onClickDismiss(key)}>
                <FormattedMessage id='button.dismiss' />
              </Button>
            )}
            anchorOrigin={{ vertical: 'bottom', horizontal: 'left' }}>
            <LocalizationProvider dateAdapter={AdapterDateFns}
              adapterLocale={DATE_LOCALE_MAP[locale]}>
              <UserContextProvider>
                <TasksSetup>
                  <Burger.Provider children={[frontdeskApp]} secondary="toolbar.articles" drawerOpen />
                </TasksSetup>
              </UserContextProvider>
            </LocalizationProvider>
          </SnackbarProvider>

        </FeedbackProvider>
      </IAPSessionRefreshContext>
    </ConfigContextProvider>
  );
}
