import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { WrenchComposerApi as Composer } from '@/wrench-setup';
import { HdesApi } from '@/api-wrench';

export const Hook = createFileFetch('worker/rest/api/assets/wrench/resources.POST')({
  hook
}) 

function hook(props: {}) {
  const params = Hook.useParams();
  const { url, method } = params;
  const headers = Composer.useQueryHeaders();

  return {
    createAsset: async(
      name: string, desc: string | undefined, 
      type: HdesApi.AstBodyType | "SITE", 
      body: HdesApi.AstCommand[] | undefined, 
      branchName: string | undefined): Promise<HdesApi.Site> => {
      return params
        .fetch(url({}), { method, body: JSON.stringify({ name, desc, type, body }), headers: { ...headers, ...( branchName ? { 'Branch-Name': branchName } : {})} })
        .then(resp => resp.json());
    }
  }
}