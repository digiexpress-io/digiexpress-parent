import { createFileFetch } from '@dxs-ts/envir-fetch';

import { HdesApi, WrenchComposerApi as Composer } from '@dxs-ts/wrench-api';

export const Hook = createFileFetch('worker/rest/api/assets/wrench/commands.POST')({
  hook
}) 

function hook(props: {}) {
  const params = Hook.useParams();
  const { url, method } = params;
  const headers = Composer.useQueryHeaders();

  

  return {
    ast: async (id: string, bodyType: HdesApi.AstBodyType, commands: HdesApi.AstCommand[] | string, branchName: string | undefined): Promise<HdesApi.Entity<any>> => {
      const payload = JSON.stringify({ 
        id, 
        bodyType, 
        bodySyntax: Array.isArray(commands) ? undefined : commands,
        bodyStatment: Array.isArray(commands) ? commands : [],
      });

      return params
        .fetch(url({}), { method, body: payload, headers: { ...headers, ...( branchName ? { 'Branch-Name': branchName } : {})} })
        .then(resp => resp.json())
        .then(data => data);
    }
  }
}