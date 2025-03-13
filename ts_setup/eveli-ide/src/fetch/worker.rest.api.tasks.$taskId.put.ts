import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { Task } from '../frontdesk/types/task/Task';

export const Hook = createFileFetch('worker/rest/api/tasks/$taskId.PUT')({
  hook
})

function hook(props: {}) {
  const params = Hook.useParams();
  const { path, contextPath, method, url } = params;

  return {

    updateTask: async (task: Task): Promise<Task> => {
      return params
        .fetch(url({ taskId: task.id! }), { method, body: JSON.stringify(task) })
        .then(response => response.json());
    }

  }
}