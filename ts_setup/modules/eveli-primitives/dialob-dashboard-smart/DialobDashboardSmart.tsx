
import React from 'react';

import { useFetch } from '@dxs-ts/envir-fetch';
import { DialobFormsProvider } from '@dxs-ts/eveli-api';
import { Box } from '@mui/material';
import { IntlProvider, useIntl } from 'react-intl';

import { messages } from './intl';
import { FormTable } from './form-table';
import { FormTableToolbar } from './form-table-toolbar';




export const DialobDashboardSmart: React.FC<{}> = () => {
  const { dialobUrl } = useFetch('dialob.GET', {});
  const { locale } = useIntl();
  
  return (
    <IntlProvider locale={locale} messages={messages[locale]}>
      <DialobFormsProvider dialobApiUrl={dialobUrl}>
        <Box sx={{ display: 'inline-block' }}>
          <FormTableToolbar />
          <FormTable />
        </Box>
      </DialobFormsProvider>
    </IntlProvider>)
}