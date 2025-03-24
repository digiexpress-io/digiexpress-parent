import { TaskApi } from '@/api-task';
import { createFileFetch } from '@dxs-ts/eveli-fetch';


export const Hook = createFileFetch('worker/rest/api/tasks/$taskId/comments.GET')({
  hook
})

function hook(props: {}) {
  const params = Hook.useParams();
  const { path, contextPath, method, url } = params;

  return {

    getTaskComments: async (task: TaskApi.Task): Promise<TaskApi.Comment[]> => {
      if (!task.id) {
        return [];
      }
      
      return params
        .fetch(url({ taskId: task.id }))
        .then(response => {
          if (response.ok) return response.json();
          throw new Error("Error with code:" + response.status);
        });
    }
  }
}