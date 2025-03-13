import { createFileFetch } from '@dxs-ts/eveli-fetch';


export const Hook = createFileFetch('worker/rest/api/feedback/$feedbackId/enabled.GET')({
  hook
})

function hook(props: {}) {

  const params = Hook.useParams();
  const { path, contextPath, method, url } = params;

  return {

    isTaskFeedbackEnabled:  async (feedbackId: string): Promise<true | false>  => {
      const service = url({ feedbackId });
      const response = await params.fetch(service, {
        method: method,
        headers: { 'Content-Type': 'application/json' },
        credentials: undefined,
      });
      return await response.json().then(json => json.enabled);
    }
  }
}