import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { WrenchComposerApi as Composer } from '@/burger'
import { HdesApi } from '@/burger';

export const Hook = createFileFetch('worker/rest/api/assets/wrench/commands.POST')({
  hook
}) 

function hook(props: {}) {
  const params = Hook.useParams();
  const { url, method } = params;
  const headers = Composer.useQueryHeaders();

  

  return {
    ast: (id: string, body: HdesApi.AstCommand[]): Promise<HdesApi.Entity<any>> => {
      return params
      .fetch(url({}), { method, body: JSON.stringify({ id, body }), headers })
      .then(resp => resp.json());
    }
  }
}