import { createFileFetch } from '@dxs-ts/envir-fetch';
import { FeedbackApi } from '@dxs-ts/task-feedback';


export const Hook = createFileFetch('worker/rest/api/feedback/$feedbackId/templates.GET')({
  hook
})

function hook(props: {}) {

  const params = Hook.useParams();
  const { path, contextPath, method, url } = params;

  return {

    getOneTemplate:  async (feedbackId: string):  Promise<FeedbackApi.FeedbackTemplate >  => {
      const service = url({ feedbackId });
      const response = await params.fetch(service, {
        method: method,
        headers: { 'Content-Type': 'application/json' },
        credentials: undefined,
      });
      return await response.json();
    }
  }
}