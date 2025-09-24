import { createFileFetch } from '@dxs-ts/envir-fetch';

import { HdesApi, WrenchComposerApi as Composer } from '@dxs-ts/wrench-api';

export const Hook = createFileFetch('worker/rest/api/assets/wrench/dataModels.GET')({
  hook
}) 

function hook(props: {}) {
  const params = Hook.useParams();
  const { url, method } = params;
  const headers = Composer.useQueryHeaders();
  

  return {
    getSite: async (branchName: string | undefined): Promise<HdesApi.Site> => {
      return params
        .fetch(url({}), { method, headers: { ...headers, ...( branchName ? { 'Branch-Name': branchName } : {})} })
        .then(resp => resp.json());
    }
  }
}