import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { WrenchComposerApi as Composer } from '@/wrench-setup';
import { HdesApi } from '@/api-wrench';

export const Hook = createFileFetch('worker/rest/api/assets/wrench/commands.POST')({
  hook
}) 

function hook(props: {}) {
  const params = Hook.useParams();
  const { url, method } = params;
  const headers = Composer.useQueryHeaders();

  

  return {
    ast: (id: string, body: HdesApi.AstCommand[], branchName: string | undefined): Promise<HdesApi.Entity<any>> => {
      return params
      .fetch(url({}), { method, body: JSON.stringify({ id, body }), headers: { ...headers, ...( branchName ? { 'Branch-Name': branchName } : {})} })
      .then(resp => resp.json())
      .then(data => data);
    }
  }
}