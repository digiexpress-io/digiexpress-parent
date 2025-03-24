import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { useQuery } from '@tanstack/react-query'

import { mapIamRole } from '@/api-iam';
import { TaskApi } from '@/api-task';


export const Hook = createFileFetch('statistics/task-overdue.GET')({
  hook
}) 

function hook(props: {}): { 
  tasksOverdue: { assignedId: string, count: number }[] | undefined
} {

  const params = Hook.useParams();
  const { url } = params;
  const query = url({});
  
  const { data, error, refetch, isPending } = useQuery({
    queryKey: [query],
    queryFn: () => params
      .fetch(query).then(resp => resp.json())
      .then((data: TaskApi.OverdueByGroupStatistics[]) => (data ?? []))
      .catch(err => [])
      .then(data => data.map(stats => ({
            assignedId: mapIamRole(stats.assignedId),
            count: stats.count
          })
        )
      ),
  });

  return { tasksOverdue: data }
}