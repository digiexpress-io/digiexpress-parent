import { createFileFetch } from '@dxs-ts/envir-fetch';
import { MetisApi } from '../api-metis';


export const Hook = createFileFetch('worker/rest/api/metis/search/status.GET')({
  hook
})

function hook(props: {}) {
  const params = Hook.useParams();
  const { url } = params;

  return {
    getMetisSearchStatus: async (): Promise<MetisApi.SearchIndexStatus | undefined> => {
      const response = await params.fetch(url({}));
      if (response.status === 404) {
        return undefined;
      }
      return response.json();
    }
  }
}
