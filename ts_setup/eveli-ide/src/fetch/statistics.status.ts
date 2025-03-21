import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { useQuery } from '@tanstack/react-query'


import { useIntl } from 'react-intl';
import { TaskApi } from '@/burger';

export const Hook = createFileFetch('statistics/status.GET')({
  hook
}) 

type StatusTitleMap = {
  [status in TaskApi.TaskStatus]: string
}
const taskStatusMapping: StatusTitleMap = {
  'NEW': 'task.status.new',
  'OPEN': 'task.status.open',
  'REJECTED': 'task.status.rejected',
  'COMPLETED': 'task.status.completed',
}

function hook(props: {}): { 
  taskStatusNames: { status: string, count: number }[] | undefined,
  taskStatusStats: TaskApi.TaskStatusStatistics[] | undefined,
  taskStatusMapping: StatusTitleMap
} {
  const params = Hook.useParams();
  const { url } = params;
  const query = url({});
  const intl = useIntl();
  
  const { data, error, refetch, isPending } = useQuery({
    queryKey: [query],
    queryFn: () => params
      .fetch(query).then(resp => resp.json())
      .then((data: TaskApi.TaskStatusStatistics[]) => (data ?? []))
      .catch(err => [])
      .then(taskStatusStats => {


        const taskStatusNames = taskStatusStats.map(stats => ({
          status: intl.formatMessage({ id: taskStatusMapping[stats.status] }),
          count: stats.count
        }))

        return { taskStatusNames, taskStatusStats }
      }),
  });

  return { 
    taskStatusNames: data?.taskStatusNames, 
    taskStatusStats: data?.taskStatusStats,
    taskStatusMapping }
}