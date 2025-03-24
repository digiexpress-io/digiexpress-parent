import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { WrenchComposerApi as Composer } from '@/burger';
import { HdesApi } from '@/burger';

export const Hook = createFileFetch('worker/rest/api/assets/wrench/summary/$tagId.GET')({
  hook
}) 

function hook(props: {}) {
  const params = Hook.useParams();
  const { url, method } = params;
  const headers = Composer.useQueryHeaders();

  return {
    summary: async(tagId: string): Promise<HdesApi.AstTagSummary> => {
      return params
        .fetch(url({ tagId }), { method, headers })
        .then(resp => resp.json());
    }
  }
}