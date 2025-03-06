import React from 'react';
import { Button, Dialog, DialogActions, DialogContent, DialogTitle } from '@mui/material';
import { useIntl, FormattedMessage } from 'react-intl';
import { QueryClientProvider, QueryClient } from '@tanstack/react-query';
import { GFormReview, DialobProvider } from '@dxs-ts/gamut';

import { useFetch } from '@dxs-ts/eveli-fetch';

export interface DialobReviewProps {
  taskId: string;
  onClose: () => void;
}

// cross reference to gamut project
// start gamut in limited scope
export const DialobReview: React.FC<DialobReviewProps> = (props) => {
  const intl = useIntl();
  const queryClient = new QueryClient()
  const { fetchReviewGet } = useFetch('worker/rest/api/tasks/$taskId/reviews.GET', {});

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
            <Button variant='contained' onClick={props.onClose}><FormattedMessage id='button.close'/></Button>
          </DialogActions>
        </Dialog>
      </DialobProvider>
    </QueryClientProvider>
  </>)
}