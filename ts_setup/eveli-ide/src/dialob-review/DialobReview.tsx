import React from 'react';
import { Dialog, DialogActions, DialogContent, DialogTitle } from '@mui/material';
import { useIntl } from 'react-intl';
import { QueryClientProvider, QueryClient } from '@tanstack/react-query';

import { GFormReview, DialobProvider, DialobApi } from '@/gamut';
import * as Burger from '@/burger';


import { useConfig } from '../frontdesk/context/ConfigContext';

export interface DialobReviewProps {
  taskId: string;
  onClose: () => void;
}

// cross reference to gamut project
// start gamut in limited scope
export const DialobReview: React.FC<DialobReviewProps> = (props) => {
  const intl = useIntl();
  const queryClient = new QueryClient()
  const { serviceUrl } = useConfig();


  const fetchReviewGet: DialobApi.FetchReviewGET = async (sessionId) => {
    const response = await window.fetch(`${serviceUrl}worker/rest/api/tasks/${sessionId}/reviews`, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' },
      credentials: undefined,
    });
    return response;
  }

  return (<>
    <QueryClientProvider client={queryClient}>
      <DialobProvider
        fetchActionGet={'not-implemented' as any}
        fetchActionPost={'not-implemented' as any}
        fetchReviewGet={fetchReviewGet}>

        <Dialog open={true} onClose={props.onClose} maxWidth='md' fullWidth>
          <DialogTitle>{intl.formatMessage({ id: 'dialobForm.review.title' })}</DialogTitle>

          <DialogContent>
            <GFormReview formId={props.taskId} />
          </DialogContent>

          <DialogActions>
            <Burger.PrimaryButton onClick={props.onClose} label='button.close' />
          </DialogActions>
        </Dialog>
      </DialobProvider>
    </QueryClientProvider>
  </>)
}