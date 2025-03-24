import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { WrenchComposerApi as Composer } from '@/wrench-setup';
import { HdesApi } from '@/api-wrench';

export const Hook = createFileFetch('worker/rest/api/assets/wrench/debugs.POST')({
  hook
}) 

function hook(props: {}) {
  const params = Hook.useParams();
  const { url, method } = params;
  const headers = Composer.useQueryHeaders();

  return {
    debug: async (debug: HdesApi.DebugRequest): Promise<HdesApi.DebugResponse> => {
      return params
        .fetch(url({}), { method, body: JSON.stringify(debug), headers })
        .then(resp => resp.json());
    }
  }
}