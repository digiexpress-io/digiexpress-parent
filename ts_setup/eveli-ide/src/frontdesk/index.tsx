import React from 'react';
import { Button } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import { UserContextProvider } from './context/UserContext';
import { ConfigContextProvider } from './context/ConfigContext';
import { IAPSessionRefreshContext } from './context/SessionRefreshContext';

import { DATE_LOCALE_MAP } from './intl/datelocalization';
import { AdapterDateFns } from "@mui/x-date-pickers/AdapterDateFnsV3";
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { SnackbarProvider } from 'notistack';
import { AppSetup } from './AppSetup';
import { LocaleSelectContextProvider, useLocaleSelect } from './context';


export { frontdeskIntl } from './intl';


export interface FrontdeskProps {
  defaultLocale?: string | undefined;
  configUrl?: string | undefined;
}



const WithLocale: React.FC = () => {
  const { locale } = useLocaleSelect();
  const notistackRef = React.createRef<SnackbarProvider>();
  const onClickDismiss = (key: string | number | undefined) => () => {
    notistackRef.current?.closeSnackbar(key);
  }

  return (
    <LocalizationProvider dateAdapter={AdapterDateFns} adapterLocale={DATE_LOCALE_MAP[locale]}>
      <IAPSessionRefreshContext>
        <SnackbarProvider maxSnack={3} ref={notistackRef}
          action={(key) => (<Button onClick={onClickDismiss(key)}><FormattedMessage id='button.dismiss' /></Button>)}
          anchorOrigin={{ vertical: 'bottom', horizontal: 'left' }}>
          <UserContextProvider>
            <AppSetup locale={locale} />
          </UserContextProvider>
        </SnackbarProvider>
      </IAPSessionRefreshContext>
    </LocalizationProvider>
  )
}

export const Frontdesk: React.FC<FrontdeskProps> = (initProps) => {
  const { defaultLocale = 'en', configUrl = '/config' } = initProps;

  return (
    <ConfigContextProvider path={configUrl}>
      <LocaleSelectContextProvider locale={defaultLocale}>
        <WithLocale />
      </LocaleSelectContextProvider>
    </ConfigContextProvider>
  );
}
