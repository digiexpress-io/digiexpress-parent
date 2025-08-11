import { createFileFetch } from '@dxs-ts/envir-fetch';
import { BatchApi } from '../api-batch';


export const Hook = createFileFetch('worker/rest/api/batches/steps/$stepId.GET')({
  hook
})

function hook(props: {}) {
  const params = Hook.useParams();
  const { url } = params;


  return {

    getOne: async (stepId: string): Promise<BatchApi.RuntimeStep> => {
      return params.fetch(url({stepId}))
        .then(response => response.json());
    },
  }
}