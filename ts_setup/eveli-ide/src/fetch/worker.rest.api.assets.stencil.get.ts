import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { StencilApi } from '@/burger';;


export const Hook = createFileFetch('worker/rest/api/assets/stencil.GET')({
  hook
}) 

function hook(props: {}) {
  const params = Hook.useParams();
  const { url, method } = params;
  
  return {
    getSite: async (): Promise<StencilApi.Site> => {
      return params
        .fetch(url({ }) + '/', { method })
        .then(resp => resp.json())
        .catch(resp => {
          // finish error handling
          const result: StencilApi.Site = {
            contentType: 'NO_CONNECTION',
            name: "not-connected",
            articles: {},
            links: {},
            locales: {},
            pages: {},
            releases: {},
            workflows: {},
            templates: {},
          };
    
          return result;
        });
    }
  }
}