import { createFileFetch } from '@dxs-ts/envir-fetch';
import { CockpitApi } from '@dxs-ts/cockpit-api';

export const Hook = createFileFetch('worker/rest/api/cockpits.POST')({
  hook
})

function hook(_props: {}) {
  const params = Hook.useParams();
  const { method, url } = params;

  return {
    createCockpit: async (command: CockpitApi.CreateCockpitCommand): Promise<CockpitApi.CockpitContainer> => {
      const props = { method, body: JSON.stringify(command) };
      return params.fetch(url({ }), props).then(response => response.json());
    }
  }
}