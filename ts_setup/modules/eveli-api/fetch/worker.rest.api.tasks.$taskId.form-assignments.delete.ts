import { TaskApi } from '@dxs-ts/task-api';
import { createFileFetch } from '@dxs-ts/envir-fetch';


export const Hook = createFileFetch('worker/rest/api/tasks/$taskId/form-assignments.DELETE')({
  hook
})

function hook(_props: {}) {
  const params = Hook.useParams();
  const { url, method } = params;

  return {
    deleteManyCustomerAssignment: async (taskId: string, assignmentIds: string[]): Promise<TaskApi.Task> => {
      return params
        .fetch(url({ taskId }), { method, body: JSON.stringify({ assignmentIds }) } )
        .then(response => {
          if (response.ok) return response.json();
          throw new Error("Error with code:" + response.status);
        });
    }
  }
}