import { createFileFetch } from '@dxs-ts/envir-fetch';
import { ROLE_AUTHORIZED } from '../api-iam';
import { TaskApi } from '@dxs-ts/task-api';

export const Hook = createFileFetch('worker/rest/api/tasks.POST')({
  hook
})

function hook(props: {}) {
  const params = Hook.useParams();
  const { path, contextPath, method, url } = params;

  return {
    createTask: async (task: TaskApi.Task): Promise<TaskApi.Task> => {
      // default label for created task
      if (!(task.keyWords && task.keyWords?.length >0)) {
        task.keyWords = ['Manual'];
      }
      // by default visible to all users
      if (!task.assignedRoles) {
        task.assignedRoles = [ROLE_AUTHORIZED];
      }
      
      return params
        .fetch(url({}), { method, body: JSON.stringify({...task, commandType: 'CreateTask' }) })
        .then(response => response.json());
    }
  }
}