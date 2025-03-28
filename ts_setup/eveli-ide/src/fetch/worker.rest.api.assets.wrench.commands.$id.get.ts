import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { WrenchComposerApi as Composer } from '@/wrench-setup';
import { HdesApi } from '@/api-wrench';

export const Hook = createFileFetch('worker/rest/api/assets/wrench/commands/$id.GET')({
  hook
}) 

function hook(props: {}) {
  const params = Hook.useParams();
  const { url, method } = params;
  const headers = Composer.useQueryHeaders();

  

  return {
    getCommands: async (id: string): Promise<HdesApi.AstCommand[]> => {
      return params
      .fetch(url({ id }), { method, headers })
      .then(resp => resp.json());
    }
  }
}