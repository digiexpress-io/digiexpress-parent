import React from 'react';
import { Button } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import { UserContextProvider } from './context/UserContext';
import { ConfigContextProvider, useConfig } from './context/ConfigContext';
import { IAPSessionRefreshContext } from './context/SessionRefreshContext';

import { DATE_LOCALE_MAP } from './intl/datelocalization';
import { AdapterDateFns } from "@mui/x-date-pickers/AdapterDateFnsV3";
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { SnackbarProvider } from 'notistack';
import { AppSetup } from './AppSetup';
import { LocaleSelectContextProvider, useLocaleSelect } from './context';
import { FeedbackProvider, FeedbackApi } from '../feedback';


export { frontdeskIntl } from './intl';



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

const WithFeedback: React.FC<{ children: React.ReactNode }> = ({ children }) => {

  const { serviceUrl } = useConfig();

  const fetchFeedbackGET: FeedbackApi.FetchFeedbackGET = async (taskId) => {
    const response = await window.fetch(`${serviceUrl}worker/rest/api/feedback${taskId ? '/' + taskId : ''}`, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' },
      credentials: undefined,
    });
    return response;
  }

  const fetchFeedbackPOST: FeedbackApi.FetchFeedbackPOST = async (taskId, command) => {
    const response = await window.fetch(`${serviceUrl}worker/rest/api/tasks/${taskId}/feedback`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: undefined,
      body: JSON.stringify(command)
    });
    return response;
  }

  const fetchTemplateGET: FeedbackApi.FetchTemplateGET = async (taskId) => {
    const response = await window.fetch(`${serviceUrl}worker/rest/api/tasks/${taskId}/feedback-templates`, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' },
      credentials: undefined,
    });
    return response;
  }

  const fetchFeedbackDELETE: FeedbackApi.FetchFeedbackDELETE = async (taskId) => {
    const response = await window.fetch(`${serviceUrl}worker/rest/api/feedback/${taskId}`, {
      method: 'DELETE',
      headers: { 'Content-Type': 'application/json' },
      credentials: undefined,
    });
    return response;
  }


  return (
    <FeedbackProvider fetchFeedbackGET={fetchFeedbackGET} fetchFeedbackPOST={fetchFeedbackPOST} fetchTemplateGET={fetchTemplateGET} fetchFeedbackDELETE={fetchFeedbackDELETE}>
      {children}
    </FeedbackProvider>
  );
}


export interface FrontdeskProps {
  defaultLocale?: string | undefined;
  configUrl?: string | undefined;
}

export const Frontdesk: React.FC<FrontdeskProps> = (initProps) => {
  const { defaultLocale = 'en', configUrl = '/config' } = initProps;

  return (
    <ConfigContextProvider path={configUrl}>
      <LocaleSelectContextProvider locale={defaultLocale}>
        <WithFeedback><WithLocale /></WithFeedback>
      </LocaleSelectContextProvider>
    </ConfigContextProvider>
  );
}
