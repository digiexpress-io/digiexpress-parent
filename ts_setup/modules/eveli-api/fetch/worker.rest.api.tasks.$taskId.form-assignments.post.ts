import { TaskApi } from '@dxs-ts/task-api';
import { createFileFetch } from '@dxs-ts/envir-fetch';


export const Hook = createFileFetch('worker/rest/api/tasks/$taskId/form-assignments.POST')({
  hook
})

function hook(props: {}) {
  const params = Hook.useParams();
  const { method, url } = params;

  return {
    createManyTaskCustomerAssignments: async (taskId: string, request: TaskApi.CreateTaskCustomerAssignmentCommand[]): Promise<TaskApi.Task> => {
      return params.fetch(url({taskId}), { method, body: JSON.stringify(request) })
        .then(response => {
          if (response.ok) return response.json();
          throw new Error("Create assignment error:" + response.status);
        })
      }

  }
}