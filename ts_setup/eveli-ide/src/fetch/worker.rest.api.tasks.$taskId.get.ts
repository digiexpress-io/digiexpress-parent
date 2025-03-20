import { TaskApi } from '@/burger';
import { createFileFetch } from '@dxs-ts/eveli-fetch';


export const Hook = createFileFetch('worker/rest/api/tasks/$taskId.GET')({
  hook
})

function hook(props: {}) {
  const params = Hook.useParams();
  const { path, contextPath, method, url } = params;

  return {

    getTask: async (taskId: string): Promise<TaskApi.Task> => {
      return params.fetch(url({ taskId }))
      .then(response => response.json())
      .then(task => {
        if (task.dueDate) {
          task.dueDate = new Date(task.dueDate);
        }
        return task;
      });
    }

  }
}