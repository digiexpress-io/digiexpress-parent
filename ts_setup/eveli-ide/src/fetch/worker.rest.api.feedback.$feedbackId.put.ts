import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { FeedbackApi } from '../feedback';


export const Hook = createFileFetch('worker/rest/api/feedback/$feedbackId.PUT')({
  hook
})

function hook(props: {}) {

  const params = Hook.useParams();
  const { path, contextPath, method, url } = params;

  return {

    modifyOneFeedback:  async (taskId: FeedbackApi.TaskId, body: FeedbackApi.ModifyOneFeedbackCommand):  Promise<FeedbackApi.Feedback>  => {
      const service = url({ feedbackId: taskId });
      const response = await params.fetch(service, {
        method: method,
        headers: { 'Content-Type': 'application/json' },
        credentials: undefined,
        body: JSON.stringify(body)
      });
      return await response.json();
    }, 
    rankOneFeedback:  async (taskId: FeedbackApi.TaskId, body: FeedbackApi.UpsertFeedbackRankingCommand):  Promise<FeedbackApi.Feedback>  => {
      const service = url({ feedbackId: taskId });
      const response = await params.fetch(service, {
        method: method,
        headers: { 'Content-Type': 'application/json' },
        credentials: undefined,
        body: JSON.stringify(body)
      });
      return await response.json();
    }, 
  }
}