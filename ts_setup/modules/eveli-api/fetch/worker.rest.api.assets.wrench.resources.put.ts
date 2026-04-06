import { createFileFetch } from '@dxs-ts/envir-fetch';
import { HdesApi, WrenchComposerApi as Composer } from '@dxs-ts/wrench-api';

export const Hook = createFileFetch('worker/rest/api/assets/wrench/resources.PUT')({
  hook
}) 

function hook(props: {}) {
  const params = Hook.useParams();
  const { url, method } = params;
  const headers = Composer.useQueryHeaders();

  return {
    update: async(id: string, bodyType: HdesApi.AstBodyType, commands: HdesApi.AstCommand[], branchName: string | undefined): Promise<HdesApi.Site> => {
      const payload = JSON.stringify({ 
        id, 
        bodyType, 
        bodySyntax: Array.isArray(commands) ? undefined : commands,
        bodyStatment: Array.isArray(commands) ? commands : [],
      });
      return params
        .fetch(url({}), { method, body: payload, headers: { ...headers, ...( branchName ? { 'Branch-Name': branchName } : {})} })
        .then(resp => resp.json());
    }
  }
}