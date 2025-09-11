import { createFileFetch } from '@dxs-ts/envir-fetch';
import { EveliHealthTaskActivity, EveliHealthUserActivity } from '../api-health';





export const Hook = createFileFetch('worker/rest/api/health.GET')({
  hook
})

function hook(props: {}) {
  const params = Hook.useParams();
  const { url } = params;

  return {

    findAllTaskActivity: async (): Promise<EveliHealthTaskActivity[]> => {
      return params.fetch(`${url({})}/task-activity`)
        .then(response => response.json());
    },
    findAllUserActivity: async (): Promise<EveliHealthUserActivity[]> => {
      return params.fetch(`${url({})}/user-activity`)
        .then(response => response.json());
    }

  }
}