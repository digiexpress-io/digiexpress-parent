import { createFileFetch } from '@dxs-ts/envir-fetch';
import { BatchApi } from '../api-batch';


export const Hook = createFileFetch('worker/rest/api/batches.GET')({
  hook
})

function hook(props: {}) {
  const params = Hook.useParams();
  const { path, contextPath, method, url } = params;


  return {
    findAll: async (): Promise<BatchApi.Batch[]> => {
      return params.fetch(url({}) )
        .then(response => response.json());
    },
    getOne: async (batchId: string): Promise<BatchApi.Batch> => {
      return params.fetch(url({}) + '/' + batchId)
        .then(response => response.json());
    },
  }
}