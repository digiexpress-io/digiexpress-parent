import { createFileFetch } from '@dxs-ts/envir-fetch';

import { HdesApi, WrenchComposerApi as Composer } from '@dxs-ts/wrench-api';

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