import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { useQuery } from '@tanstack/react-query'


import { useIntl } from 'react-intl';

import { TaskApi } from '@/burger';





export const Hook = createFileFetch('statistics/priority.GET')({
  hook
}) 


type PriorityTitleMap = {
  [priority in TaskApi.TaskPriority]: string
}

const taskPriorityMapping: PriorityTitleMap = {
  'LOW': 'task.priority.low',
  'NORMAL': 'task.priority.normal',
  'HIGH': 'task.priority.high',
}

function hook(props: {}): { 
  taskPriorityNames: { priority: string, count: number }[] | undefined;
  taskPriorityStats: TaskApi.TaskPriorityStatistics[] | undefined;
  taskPriorityMapping: PriorityTitleMap;
} {

  const params = Hook.useParams();
  const { url } = params;
  const query = url({});
  const intl = useIntl();
  
  const { data, error, refetch, isPending } = useQuery({
    queryKey: [query],
    queryFn: () => params
      .fetch(query).then(resp => resp.json())
      .then((data: TaskApi.TaskPriorityStatistics[]) => (data ?? []))
      .catch(_err => [])
      .then(taskPriorityStats => {
        return {
          taskPriorityStats,
          taskPriorityNames: taskPriorityStats.map(stats => ({
            priority: intl.formatMessage({ id: taskPriorityMapping[stats.priority] }),
            count: stats.count
          }))
        }
      }),
  });

  return { 
    taskPriorityNames: data?.taskPriorityNames, 
    taskPriorityStats: data?.taskPriorityStats,
    taskPriorityMapping }
}