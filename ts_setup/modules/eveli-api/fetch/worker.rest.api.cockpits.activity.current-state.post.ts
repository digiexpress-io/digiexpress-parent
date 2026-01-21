import { createFileFetch } from '@dxs-ts/envir-fetch';
import { CockpitApi } from '@dxs-ts/cockpit-api';

export const Hook = createFileFetch('worker/rest/api/cockpits/activity/current-state.POST')({
  hook
})

function hook(_props: {}) {
  const params = Hook.useParams();
  const { method, url } = params;

  return {
    changeActivity: async (command: CockpitApi.CockpitActivityChangeActiveIdCommand): Promise<CockpitApi.CockpitActivity> => {
      const props = { method, body: JSON.stringify(command) };
      return params.fetch(url({ }), props).then(response => response.json());
    }
  }
}