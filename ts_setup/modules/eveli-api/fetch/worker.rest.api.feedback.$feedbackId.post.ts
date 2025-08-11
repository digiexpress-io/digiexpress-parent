import { createFileFetch } from '@dxs-ts/envir-fetch';
import { FeedbackApi } from '../api-feedback';


export const Hook = createFileFetch('worker/rest/api/feedback/$feedbackId.POST')({
  hook
})

function hook(props: {}) {

  const params = Hook.useParams();
  const { path, contextPath, method, url } = params;

  return {

    createOneFeedback:  async (taskId: FeedbackApi.TaskId, command: FeedbackApi.CreateFeedbackCommand): Promise<FeedbackApi.Feedback>  => {
      const service = url({ feedbackId: taskId });
      const response = await params.fetch(service, {
        method: method,
        headers: { 'Content-Type': 'application/json' },
        credentials: undefined,
        body: JSON.stringify(command)
      });
      return await response.json();
    }
  }
}