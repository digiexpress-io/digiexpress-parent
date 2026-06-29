import React from 'react';
import { createTheme, ThemeProvider } from '@mui/material';
import { useIntl } from 'react-intl';
import { useFetch } from '@dxs-ts/envir-fetch';

import { DialobProvider, WithFormProvider, LocaleProvider } from '@dxs-ts/gamut-api';
import { GFormTip } from '@dxs-ts/gamut-form';
import { GThemeOptions } from '@dxs-ts/gamut-theme';
import { useNavigate } from '@tanstack/react-router';

import { QueryClient, QueryClientProvider } from '@tanstack/react-query';

interface InHouseFillProps {
  workflowId: string;
  locale: string;
  onCancel: () => void;
}



const queryClient = new QueryClient();
const reviewTheme = createTheme(GThemeOptions);
const formUnavailable = () => {
  return 'form is unavailable';
}

export const InHouseFill: React.FC<InHouseFillProps> = ({ workflowId }) => {
  const intl = useIntl();
  const dialob = useFetch('worker/rest/api/tasks/in-house/$id.GET', {})
  const nav = useNavigate();

  function handleOnComplete() {
    nav({
      from: '/secured/$locale/worker',
      to: '/secured/$locale/worker/tasks'
    })
  }

  function handleOnCancel() {

  }

  return (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider theme={reviewTheme}>
        <DialobProvider
          fetchActionGet={dialob.fetchActionGet}
          fetchActionPost={dialob.fetchActionPost}
          fetchAttachmentPost={dialob.fetchAttachmentPost}
          fetchReviewGet={dialob.fetchReviewGet}>

          <LocaleProvider defaultLocale={() => intl.locale}>
            <WithFormProvider id={workflowId} executionId={workflowId} variant='' onAfterComplete={handleOnComplete} onCancel={handleOnCancel} formUnavailable={formUnavailable}>
              <GFormTip executionId={workflowId} variant='' onAfterComplete={handleOnComplete} onCancel={handleOnCancel} formUnavailable={formUnavailable} />
            </WithFormProvider>
          </LocaleProvider>

        </DialobProvider>
      </ThemeProvider>
    </QueryClientProvider>)
};
