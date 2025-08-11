import { createFileFetch } from '@dxs-ts/envir-fetch';
import { HdesApi, WrenchComposerApi as Composer } from '@dxs-ts/wrench-api';

export const Hook = createFileFetch('worker/rest/api/assets/wrench/version.GET')({
  hook
}) 

function hook(props: {}) {
  const params = Hook.useParams();
  const { url, method } = params;
  const headers = Composer.useQueryHeaders();

  return {
    version: async(): Promise<HdesApi.VersionEntity> => {
      return params
        .fetch(url({}), { method, headers })
        .then(resp => resp.json());
    }
  }
}