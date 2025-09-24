import React from 'react';
import { QueryClientProvider, QueryClient } from '@tanstack/react-query';

import { GFormReview } from '@dxs-ts/gamut-form-review';
import { DialobProvider } from '@dxs-ts/gamut-api';

import { useFetch } from '@dxs-ts/envir-fetch';
import { DialobReviewProps } from './dialob-review-types';


// cross reference to gamut project
// start gamut in limited scope
export const DialobReview: React.FC<DialobReviewProps> = (props) => {

  const queryClient = new QueryClient()
  const { fetchReviewGet } = useFetch('worker/rest/api/tasks/$taskId/reviews.GET', {});

  return (<>
    <QueryClientProvider client={queryClient}>
      
      <DialobProvider
        fetchActionGet={'not-implemented' as any}
        fetchActionPost={'not-implemented' as any}
        fetchAttachmentPost={'not-implemented' as any}
        fetchReviewGet={fetchReviewGet}>

        <GFormReview formId={props.taskId} />
      </DialobProvider>

    </QueryClientProvider>
  </>)
}