import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { WrenchComposerApi as Composer } from '@/wrench-setup';
import { HdesApi } from '@/api-wrench';

export const Hook = createFileFetch('worker/rest/api/assets/wrench/resources/$id.DELETE')({
  hook
}) 

function hook(props: {}) {
  const params = Hook.useParams();
  const { url, method } = params;
  const headers = Composer.useQueryHeaders();

  return {
    remove: async (id: string, branchName: string | undefined): Promise<HdesApi.Site> => {
      return params
        .fetch(url({id}), { method, headers: { ...headers, ...( branchName ? { 'Branch-Name': branchName } : {})} })
        .then(resp => resp.json());
    }
  }
}