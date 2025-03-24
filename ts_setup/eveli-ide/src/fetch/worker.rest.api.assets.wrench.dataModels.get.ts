import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { WrenchComposerApi as Composer } from '@/burger';
import { HdesApi } from '@/burger';

export const Hook = createFileFetch('worker/rest/api/assets/wrench/dataModels.GET')({
  hook
}) 

function hook(props: {}) {
  const params = Hook.useParams();
  const { url, method } = params;
  const headers = Composer.useQueryHeaders();
  

  return {
    getSite: async (): Promise<HdesApi.Site> => {
      return params
        .fetch(url({}), { method, headers })
        .then(resp => resp.json());
    }
  }
}