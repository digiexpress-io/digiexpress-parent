import { createFileFetch } from '@dxs-ts/envir-fetch';
import { TaskApi } from '@dxs-ts/task-api';
import { useIntl } from 'react-intl';


export const Hook = createFileFetch('worker/rest/api/tasks.GET')({
  hook
})

function hook(props: {}) {
  const params = Hook.useParams();
  const { path, contextPath, method, url } = params;
  const intl = useIntl();

  return {
    dashboard: async (): Promise<TaskApi.TaskDasboard> => {
      return params.fetch(url({}) + `/dashboard`)
        .then(response => response.json());
    },

    findAll: async (): Promise<TaskApi.Task[]> => {
      return params.fetch(url({}) + `/all`)
        .then(response => response.json())
        .then((tasks: TaskApi.Task[]) => tasks.map(task => {

          return {
            ...task,
            priorityIntl: intl.formatMessage({ id: `task.priority.${task.priority?.toLowerCase()}`, defaultMessage: task.priority }),
            statusIntl: intl.formatMessage({ id: `task.status.${task.status?.toLowerCase()}`, defaultMessage: task.status })
          }
        }));
    }
  }
}