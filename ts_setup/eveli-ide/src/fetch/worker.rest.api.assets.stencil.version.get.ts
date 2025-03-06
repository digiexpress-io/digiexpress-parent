import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { Composer, StencilApi } from '../stencil';


export const Hook = createFileFetch('worker/rest/api/assets/stencil/version.GET')({
  hook
}) 

function hook(props: {}) {
  const params = Hook.useParams();
  const { url, method } = params;
  
  return {
    version: async (): Promise<StencilApi.VersionEntity> => {
      return params.fetch(url({ }), { method })
        .then(resp => resp.json())
    }
  }
}