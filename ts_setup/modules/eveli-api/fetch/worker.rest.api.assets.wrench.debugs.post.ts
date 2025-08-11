import { createFileFetch } from '@dxs-ts/envir-fetch';

import { HdesApi, WrenchComposerApi as Composer } from '@dxs-ts/wrench-api';

export const Hook = createFileFetch('worker/rest/api/assets/wrench/debugs.POST')({
  hook
}) 

function hook(props: {}) {
  const params = Hook.useParams();
  const { url, method } = params;
  const headers = Composer.useQueryHeaders();

  return {
    debug: async (debug: HdesApi.DebugRequest, branchName: string | undefined): Promise<HdesApi.DebugResponse> => {
      return params
        .fetch(url({}), { method, body: JSON.stringify(debug), headers: { ...headers, ...( branchName ? { 'Branch-Name': branchName } : {})} })
        .then(resp => resp.json());
    }
  }
}