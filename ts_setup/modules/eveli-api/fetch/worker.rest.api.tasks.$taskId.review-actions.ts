import { createFileFetch } from '@dxs-ts/envir-fetch';


export const Hook = createFileFetch('worker/rest/api/tasks/$taskId/review-actions.GET')({
  hook
}) 

function hook(props: {}) {
  const params = Hook.useParams();
  const { method, url } = params;
  

  return {
    fetchReviewActionsGet: async (sessionId: string) => {
      const serviceUrl = url({ taskId: sessionId });
      const response = await params.fetch(serviceUrl, {
        method: method,
        headers: undefined,
        credentials: undefined,
      })
    
      return response;
    },
    fetchReviewActionsPost: async (sessionId: string, actions: any[], rev: number) => {
      const serviceUrl = url({ taskId: sessionId });
      const response = await params.fetch(serviceUrl, {
        method: 'POST',
        body: JSON.stringify({ rev, actions }),

        headers: undefined,
        credentials: undefined,
      })

      return response;
    }
  }
}