import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { WrenchComposerApi as Composer } from '@/wrench-setup';
import { HdesApi } from '@/api-wrench';

export const Hook = createFileFetch('worker/rest/api/assets/wrench/diff.GET')({
  hook
}) 

function hook(props: {}) {
  const params = Hook.useParams();
  const { url, method } = params;
  const headers = Composer.useQueryHeaders();

  return {
    diff: async(input: HdesApi.DiffRequest): Promise<HdesApi.DiffResponse> => {
      return params
        .fetch(`${url({})}?baseId=${input.baseId}&targetId=${input.targetId}`, { method, headers })
        .then(resp => resp.json());
    }
  }
}