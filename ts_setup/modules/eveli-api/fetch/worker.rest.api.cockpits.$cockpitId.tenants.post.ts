import { createFileFetch } from '@dxs-ts/envir-fetch';
import { CockpitApi } from '@dxs-ts/cockpit-api';

export const Hook = createFileFetch('worker/rest/api/cockpits/$cockpitId/tenants.POST')({
  hook
})

function hook(_props: {}) {
  const params = Hook.useParams();
  const { method, url } = params;

  return {
    createCockpitTenant: async (cockpitId: string, command: CockpitApi.CreateCockpitTenantCommand): Promise<CockpitApi.CockpitContainer> => {
      const props = { method, body: JSON.stringify(command) };
      return params.fetch(url({ cockpitId }), props).then(response => response.json());
    }
  }
}