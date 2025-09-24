import { createFileFetch } from '@dxs-ts/envir-fetch';
import { BatchApi } from '../api-batch';


export const Hook = createFileFetch('worker/rest/api/batches/$batchName/instances.POST')({
  hook
})

function hook(props: {}) {
  const params = Hook.useParams();
  const { path, contextPath, method, url } = params;

  return {
    createInstance: async (batchName: string, command: BatchApi.CreateOneInstanceCommand): Promise<BatchApi.RuntimeInstance> => {
      const props = { method, body: JSON.stringify(command) };
      return params.fetch(url({ batchName }), props).then(response => response.json());
    }
  }
}