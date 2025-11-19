import React from 'react';
import { createTheme, ThemeProvider } from '@mui/material';
import { useIntl } from 'react-intl';
import { QueryClientProvider, QueryClient } from '@tanstack/react-query';

import { DialobProvider, WithFormProvider, LocaleProvider } from '@dxs-ts/gamut-api';
import { GFormTip } from '@dxs-ts/gamut-form';
import { GThemeOptions } from '@dxs-ts/gamut-theme';

import { DialobReviewProps } from './dialob-review-types';
import { useDialobReviewNav } from './dialob-review-nav';

const queryClient = new QueryClient();
const reviewTheme = createTheme(GThemeOptions);
const formUnavailable = () => {
  return 'form is unavailable';
}

// cross reference to gamut project
// start gamut in limited scope

export const DialobReviewBasedOnForm: React.FC<DialobReviewProps> = (props) => {
  const intl = useIntl();
  const { fetchActionPost, fetchActionGet } = useDialobReviewNav();

  function handleOnComplete() {
    props.onClose();
  }
  function handleOnCancel() {
    props.onClose();
  }

  return (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider theme={reviewTheme}>
        <DialobProvider
          fetchActionGet={fetchActionGet}
          fetchActionPost={fetchActionPost}
          fetchAttachmentPost={'not-implemented' as any}
          fetchReviewGet={'not-implemented' as any}>

          <LocaleProvider disableErrors defaultLocale={() => intl.locale}>
            <WithFormProvider id={props.taskId} executionId={props.taskId} variant='' onAfterComplete={handleOnComplete} disabled onCancel={handleOnCancel} formUnavailable={formUnavailable}>
              <GFormTip executionId={props.taskId} variant='' onAfterComplete={handleOnComplete} onCancel={handleOnCancel} formUnavailable={formUnavailable} />
            </WithFormProvider>
          </LocaleProvider>

        </DialobProvider>
      </ThemeProvider>
    </QueryClientProvider>)
}