import { createFileFetch } from '@dxs-ts/envir-fetch';
import { MetisApi } from '../api-metis';


export const Hook = createFileFetch('worker/rest/api/metis/search/reindex/cancel.POST')({
  hook
})

function hook(props: {}) {
  const params = Hook.useParams();
  const { method, url } = params;

  return {
    cancelMetisReindex: async (): Promise<MetisApi.SearchIndexStatus | undefined> => {
      const response = await params.fetch(url({}), {
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
