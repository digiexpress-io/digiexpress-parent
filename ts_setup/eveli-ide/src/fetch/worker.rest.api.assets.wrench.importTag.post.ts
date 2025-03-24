import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { WrenchComposerApi as Composer } from '@/burger';
import { HdesApi } from '@/burger';

export const Hook = createFileFetch('worker/rest/api/assets/wrench/importTag.POST')({
  hook
}) 

function hook(props: {}) {
  const params = Hook.useParams();
  const { url, method } = params;
  const headers = Composer.useQueryHeaders();

  return {
    importTag: async (tagContentAsString: string): Promise<HdesApi.Site> => {
      return params
        .fetch(url({}), { method, body: tagContentAsString, headers })
        .then(resp => resp.json());
    }
  }
}