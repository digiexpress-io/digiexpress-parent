import { createFileFetch } from '@dxs-ts/envir-fetch';
import { CockpitApi } from '@dxs-ts/cockpit-api';

export const Hook = createFileFetch('worker/rest/api/cockpits/$cockpitId.GET')({
  hook
})

function hook(_props: {}) {
  const params = Hook.useParams();
  const { url } = params;

  return {
    getOneCockpit: async (cockpitId: string): Promise<CockpitApi.CockpitSummary> => {
      return params.fetch(url({ cockpitId }))
        .then(response => response.json())
        .then((data: CockpitApi.CockpitSummary) => {
          return data;
        })
    }
  }
}