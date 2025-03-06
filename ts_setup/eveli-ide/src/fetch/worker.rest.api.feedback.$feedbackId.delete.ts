import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { FeedbackApi } from '../feedback';


export const Hook = createFileFetch('worker/rest/api/feedback/$feedbackId.DELETE')({
  hook
})

function hook(props: {}) {

  const params = Hook.useParams();
  const { path, contextPath, method, url } = params;

  return {

    deleteOneFeedback:  async (taskId: FeedbackApi.TaskId):  Promise<FeedbackApi.Feedback>  => {
      const service = url({ feedbackId: taskId });
      const response = await params.fetch(service, {
        method: method,
        headers: { 'Content-Type': 'application/json' },
        credentials: undefined,
      });
      return await response.json();
    }
  }
}