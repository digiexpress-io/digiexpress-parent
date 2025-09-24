import { createFileFetch } from '@dxs-ts/envir-fetch';
import { TaskApi } from '@dxs-ts/task-api';


export const Hook = createFileFetch('worker/rest/api/tasks/$taskId/audits.GET')({
  hook
})

function hook(props: {}) {
  const params = Hook.useParams();
  const { method, url } = params;


  return {
    getOneTaskAudit: async (taskId: string): Promise<TaskApi.TaskAuditLog> => {
      return params.fetch(url({ taskId })).then(response => response.json());
    }
  }
}