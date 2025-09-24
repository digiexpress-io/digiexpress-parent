import { createFileFetch } from '@dxs-ts/envir-fetch';
import { FeedbackApi } from '@dxs-ts/task-feedback';


export const Hook = createFileFetch('worker/rest/api/feedback/$feedbackId.GET')({
  hook
})

function hook(props: {}) {

  const params = Hook.useParams();
  const { method, url } = params;

  return {

    getOneFeedback:  async (feedbackId: string): Promise<FeedbackApi.Feedback | undefined>  => {
      const service = url({ feedbackId });
      const response = await params.fetch(service, {
        method: method,
        headers: { 'Content-Type': 'application/json' },
        credentials: undefined,
      });

      if(response.status === 404) {
        return undefined;
      }

      return await response.json();
    }
  }
}