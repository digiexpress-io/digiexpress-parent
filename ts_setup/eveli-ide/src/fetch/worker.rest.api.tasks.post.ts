import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { Task } from '../frontdesk/types/task/Task';
import { ROLE_AUTHORIZED } from '@/burger';

export const Hook = createFileFetch('worker/rest/api/tasks.POST')({
  hook
})

function hook(props: {}) {
  const params = Hook.useParams();
  const { path, contextPath, method, url } = params;

  return {
    createTask: async (task: Task): Promise<Task> => {
      // default label for created task
      if (!(task.keyWords && task.keyWords?.length >0)) {
        task.keyWords = ['Manual'];
      }
      // by default visible to all users
      if (!task.assignedRoles) {
        task.assignedRoles = [ROLE_AUTHORIZED];
      }
      
      return params
        .fetch(url({}), { method, body: JSON.stringify(task) })
        .then(response => response.json());
    }
  }
}