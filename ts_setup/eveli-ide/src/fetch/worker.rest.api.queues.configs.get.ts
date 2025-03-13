import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { QueueApi } from '../queue';


export const Hook = createFileFetch('worker/rest/api/queues/configs.GET')({
  hook
})

function hook(props: {}) {

  const params = Hook.useParams();
  const { path, contextPath, method, url } = params;

  return {
    getOneChannelConfig:  async ():  Promise<QueueApi.ChannelConfig>  => {
      const service = url({ });
      const response = await params.fetch(service, {
        method: method,
        headers: { 'Content-Type': 'application/json' },
        credentials: undefined,
      });
      return await response.json()
        .then(data => data as QueueApi.ChannelConfig[])
        .then(([data]) => data);
    }
  }
}