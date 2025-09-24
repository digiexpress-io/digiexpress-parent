import { createFileFetch } from '@dxs-ts/envir-fetch';

import { HdesApi, WrenchComposerApi as Composer } from '@dxs-ts/wrench-api';

export const Hook = createFileFetch('worker/rest/api/assets/wrench/copyas.POST')({
  hook
}) 

function hook(props: {}) {
  const params = Hook.useParams();
  const { url, method } = params;
  const headers = Composer.useQueryHeaders();
  

  return {
    copy: async(id: string, name: string, branchName: string | undefined): Promise<HdesApi.Site> => {
      return params
        .fetch(url({}), { method, body: JSON.stringify({ id, name }), headers: { ...headers, ...( branchName ? { 'Branch-Name': branchName } : {})} })
        .then(resp => resp.json());
    }
  }
}