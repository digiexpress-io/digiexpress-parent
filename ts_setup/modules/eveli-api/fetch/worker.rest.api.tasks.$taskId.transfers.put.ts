import { TaskApi } from '../api-task';
import { createFileFetch } from '@dxs-ts/envir-fetch';

export const Hook = createFileFetch('worker/rest/api/tasks/$taskId/transfers.PUT')({
  hook
})

function hook(props: {}) {
  const params = Hook.useParams();
  const { path, contextPath, method, url } = params;

  return {
    transferTask: async (task: TaskApi.Task, command: TaskApi.TransferTaskCommand): Promise<TaskApi.Task> => {
      return params
        .fetch(url({ taskId: task.id! }), { method, body: JSON.stringify(command) })
        .then(response => response.json());
    }

  }
}