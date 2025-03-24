import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { WrenchComposerApi as Composer } from '@/burger';
import { HdesApi } from '@/burger';

export const Hook = createFileFetch('worker/rest/api/assets/wrench/resources/$id.DELETE')({
  hook
}) 

function hook(props: {}) {
  const params = Hook.useParams();
  const { url, method } = params;
  const headers = Composer.useQueryHeaders();

  return {
    remove: async (id: string): Promise<HdesApi.Site> => {
      return params
        .fetch(url({id}), { method, headers })
        .then(resp => resp.json());
    }
  }
}