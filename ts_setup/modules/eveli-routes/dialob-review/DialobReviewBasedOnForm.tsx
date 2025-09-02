import React from 'react';
import { createTheme, ThemeProvider } from '@mui/material';
import { useIntl } from 'react-intl';
import { QueryClientProvider, QueryClient } from '@tanstack/react-query';

import { DialobProvider, DialobApi, WithFormProvider, LocaleProvider } from '@dxs-ts/gamut-api';
import { GFormTip } from '@dxs-ts/gamut-form';
import { GThemeOptions } from '@dxs-ts/gamut-theme';
import { useFetch } from '@dxs-ts/envir-fetch';

import { DialobReviewProps } from './dialob-review-types';




const reviewTheme = createTheme(GThemeOptions);
const formUnavailable = () => {
  return 'form is unavailable';
}

// cross reference to gamut project
// start gamut in limited scope
export const DialobReviewBasedOnForm: React.FC<DialobReviewProps> = (props) => {
  const intl = useIntl();
  const queryClient = new QueryClient()
  const { fetchReviewActionsGet } = useFetch('worker/rest/api/tasks/$taskId/review-actions.GET', {});

  const fetchActionGet: DialobApi.FetchActionGET = async (_sessionId: string) => {
    
    return fetchReviewActionsGet(props.taskId)
      .then((data) => {
        console.log(data);
        return data;
      });
  }
  function handleOnComplete() {

  }
  function handleOnCancel() {

  }

  return (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider theme={reviewTheme}>
        <DialobProvider
          fetchActionGet={fetchActionGet}
          fetchActionPost={'not-implemented' as any}
          fetchAttachmentPost={'not-implemented' as any}
          fetchReviewGet={'not-implemented' as any}>

          <LocaleProvider disableErrors defaultLocale={() => intl.locale}>
            <WithFormProvider id='' executionId='' variant='' onAfterComplete={handleOnComplete} disabled onCancel={handleOnCancel} formUnavailable={formUnavailable}>
              <GFormTip executionId='' variant='' onAfterComplete={handleOnComplete} onCancel={handleOnCancel} formUnavailable={formUnavailable}/>
            </WithFormProvider>
          </LocaleProvider>

        </DialobProvider>
      </ThemeProvider>
    </QueryClientProvider>)
}