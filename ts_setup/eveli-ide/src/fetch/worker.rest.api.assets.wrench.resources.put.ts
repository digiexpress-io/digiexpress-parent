import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { Composer } from '../wrench';
import { HdesApi } from '@/burger';

export const Hook = createFileFetch('worker/rest/api/assets/wrench/resources.PUT')({
  hook
}) 

function hook(props: {}) {
  const params = Hook.useParams();
  const { url, method } = params;
  const headers = Composer.useQueryHeaders();

  return {
    update: async(id: string, body: HdesApi.AstCommand[]): Promise<HdesApi.Site> => {
      return params
        .fetch(url({}), { method, body: JSON.stringify({ id, body }), headers })
        .then(resp => resp.json());
    }
  }
}