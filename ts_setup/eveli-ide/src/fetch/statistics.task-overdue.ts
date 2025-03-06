import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { useQuery } from '@tanstack/react-query'

import { OverdueByGroupStatistics } from '../frontdesk/types/TaskStatistics';
import { mapIamRole } from '@/burger';


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
      .then((data: OverdueByGroupStatistics[]) => (data ?? []))
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