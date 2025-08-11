import { createFileFetch } from '@dxs-ts/envir-fetch';
import { QueueApi } from '../api-queue';


export const Hook = createFileFetch('worker/rest/api/queues/deliveries.GET')({
  hook
})

function hook(props: {}) {

  const params = Hook.useParams();
  const { path, contextPath, method, url } = params;


  return {

    findAllQueueDeliveries:  async ():  Promise<QueueApi.Delivery[]>  => {
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