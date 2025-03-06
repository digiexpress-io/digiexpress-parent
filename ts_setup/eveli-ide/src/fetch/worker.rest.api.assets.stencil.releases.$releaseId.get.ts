import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { Composer, StencilApi } from '../stencil';


export const Hook = createFileFetch('worker/rest/api/assets/stencil/releases/$releaseId.GET')({
  hook
}) 

function hook(props: {}) {
  const params = Hook.useParams();
  const { url, method } = params;
  
  return {
    getReleaseContent: async (releaseId: StencilApi.ReleaseId): Promise<{}> => {

      return params
        .fetch(url({ releaseId }), { method })
        .then(resp => resp.json())
    },
  }
}