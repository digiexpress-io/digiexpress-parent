import { createFileFetch } from '@dxs-ts/envir-fetch';
import { StencilApi } from '@dxs-ts/stencil-api';;


export const Hook = createFileFetch('worker/rest/api/assets/stencil/commitlogs.GET')({
  hook
}) 

function hook(props: {}) {
  const params = Hook.useParams();
  const { url, method } = params;
  
  return {
    getSiteCommitLog: async (): Promise<StencilApi.SiteCommitLog[]> => {
      return params
        .fetch(url({ }) , { method })
        .then(resp => resp.json())
        .catch(resp => {

          return [];
        });
    }
  }
}