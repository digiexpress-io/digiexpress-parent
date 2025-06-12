import React from 'react';
import { Button, Dialog, DialogActions, DialogContent, DialogTitle } from '@mui/material';
import { useIntl, FormattedMessage, IntlProvider } from 'react-intl';
import { QueryClientProvider, QueryClient } from '@tanstack/react-query';
import ArrowRightIcon from '@mui/icons-material/ArrowRight';

import { DialobProvider, DialobApi, WithFormProvider, GFormTip, LocaleProvider } from '@dxs-ts/gamut';

import { useFetch } from '@dxs-ts/eveli-fetch';
import { DialobReviewProps } from './dialob-review-types';



// cross reference to gamut project
// start gamut in limited scope
export const DialobReviewBasedOnForm: React.FC<DialobReviewProps> = (props) => {
  const intl = useIntl();
  const queryClient = new QueryClient()
  const { fetchReviewActionsGet } = useFetch('worker/rest/api/tasks/$taskId/review-actions.GET', {});
  const { pdfTaskLinkCallback } = useFetch('worker/rest/api/pdf.GET', {});

  const fetchActionGet: DialobApi.FetchActionGET = async (_sessionId: string) => {
    
    return fetchReviewActionsGet(props.taskId)
      .then((data) => {
        console.log(data);
        return data;
      });
  }
  function handleOnComplete() {

  }

  return (<>
    <QueryClientProvider client={queryClient}>
      
      <DialobProvider
        fetchActionGet={fetchActionGet}
        fetchActionPost={'not-implemented' as any}
        fetchAttachmentPost={'not-implemented' as any}
        fetchReviewGet={'not-implemented' as any}>

        <Dialog open={true} onClose={props.onClose} maxWidth='lg' fullWidth>
          <DialogTitle>{intl.formatMessage({ id: 'dialobForm.review.title' })}</DialogTitle>

          <DialogContent>
            <LocaleProvider disableErrors defaultLocale={() => intl.locale}>
              <WithFormProvider id='' executionId='' variant='' onAfterComplete={handleOnComplete}>
                <GFormTip executionId='' variant='' onAfterComplete={handleOnComplete} />
              </WithFormProvider>
            </LocaleProvider>
          </DialogContent>

          <DialogActions>
            <Button variant='outlined' endIcon={<ArrowRightIcon/>} onClick={()=> pdfTaskLinkCallback(props.questionnaireId, props.taskId)}>
              <FormattedMessage id='taskLink.pdf.open' />
            </Button>
            <Button variant='contained' onClick={props.onClose}><FormattedMessage id='button.close'/></Button>
          </DialogActions>
        </Dialog>
      </DialobProvider>

    </QueryClientProvider>
  </>)
}