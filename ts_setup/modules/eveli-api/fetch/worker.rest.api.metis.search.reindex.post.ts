import { createFileFetch } from '@dxs-ts/envir-fetch';
import { MetisApi } from '../api-metis';


export const Hook = createFileFetch('worker/rest/api/metis/search/reindex.POST')({
  hook
})

function hook(props: {}) {
  const params = Hook.useParams();
  const { method, url } = params;

  return {
    startMetisReindex: async (command: MetisApi.ReindexCommand = {}): Promise<MetisApi.SearchIndexStatus | undefined> => {
      const query = new URLSearchParams();
      query.set('force', String(!!command.force));
      query.set('replace', String(!!command.replace));
      const response = await params.fetch(`${url({})}?${query.toString()}`, {
        method,
        body: '{}'
      });
      if (response.status === 404) {
        return undefined;
      }
      if (response.status === 409) {
        return response.json();
      }
      return response.json();
    }
  }
}
