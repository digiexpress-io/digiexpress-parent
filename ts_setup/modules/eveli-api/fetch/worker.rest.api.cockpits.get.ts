import { createFileFetch } from '@dxs-ts/envir-fetch';
import { CockpitApi } from '@dxs-ts/cockpit-api';

export const Hook = createFileFetch('worker/rest/api/cockpits.GET')({
  hook
})

function hook(_props: {}) {
  const params = Hook.useParams();
  const { url } = params;
  
  return {
    findAllCockpits: async (): Promise<CockpitApi.CockpitSummary[]> => {
      return params.fetch(`${url({})}`)
        .then(response => response.json())
        .then((data: CockpitApi.CockpitSummary[]) => {
          return data;
        })
    },
  }
}