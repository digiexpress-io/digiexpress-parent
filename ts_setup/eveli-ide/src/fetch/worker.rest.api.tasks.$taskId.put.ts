import { TaskApi } from '@/api-task';
import { createFileFetch } from '@dxs-ts/eveli-fetch';

export const Hook = createFileFetch('worker/rest/api/tasks/$taskId.PUT')({
  hook
})

function hook(props: {}) {
  const params = Hook.useParams();
  const { path, contextPath, method, url } = params;

  return {

    updateTask: async (task: Partial<TaskApi.Task>): Promise<TaskApi.Task> => {
      return params
        .fetch(url({ taskId: task.id! }), { method, body: JSON.stringify({ ... task, commandType: 'ModifyTask'}) })
        .then(response => response.json());
    }

  }
}