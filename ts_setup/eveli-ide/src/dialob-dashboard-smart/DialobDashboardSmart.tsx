
import React from 'react';

import { useFetch } from '@dxs-ts/eveli-fetch';
import { Box } from '@mui/material';
import { IntlProvider, useIntl } from 'react-intl';

import { DialobFormsProvider } from '@/api-dialob-form';
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