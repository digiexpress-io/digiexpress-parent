import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { FeedbackApi } from '../api-feedback';


export const Hook = createFileFetch('worker/rest/api/feedback.GET')({
  hook
})

function hook(props: {}) {

  const params = Hook.useParams();
  const { path, contextPath, method, url } = params;

  return {

    findAllFeedback:  async ():  Promise<FeedbackApi.Feedback[]>  => {
      const service = url({ });
      const response = await params.fetch(service, {
        method: method,
        headers: { 'Content-Type': 'application/json' },
        credentials: undefined,
      });
      return await response.json();
    }
  }
}