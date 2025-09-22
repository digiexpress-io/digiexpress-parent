import { TaskApi } from '@dxs-ts/task-api';
import { createFileFetch } from '@dxs-ts/envir-fetch';


export const Hook = createFileFetch('worker/rest/api/tasks/$taskId/form-assignments.GET')({
  hook
})

function hook(props: {}) {
  const params = Hook.useParams();
  const { path, contextPath, method, url } = params;

  return {

    getTaskFormAssignment: async (taskId: string): Promise<TaskApi.FormAssignment[]> => {
      if (!taskId) {
        return [];
      }
      
      return params
        .fetch(url({ taskId }))
        .then(response => {
          if (response.ok) return response.json();
          throw new Error("Error with code:" + response.status);
        });
    }
  }
}