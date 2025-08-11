import { createFileFetch } from '@dxs-ts/envir-fetch';
import { QueueApi } from '../api-queue';

export const Hook = createFileFetch('worker/rest/api/queues/messages.GET')({
  hook
})

function hook(props: {}) {

  const params = Hook.useParams();
  const { path, contextPath, method, url } = params;
  return {

    findAllQueueMessages:  async ():  Promise<QueueApi.QueueMessage[]>  => {
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