import { TaskApi } from '@/burger';
import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { useQuery } from '@tanstack/react-query'
import { useIntl } from 'react-intl';


export const Hook = createFileFetch('statistics/status-timeline.GET')({
  hook
}) 

function hook(props: {}): { taskTimelineStats: TaskApi.TaskStatusTimelineStatistics[] | undefined } {

  const params = Hook.useParams();
  const { url } = params;
  const query = url({});
  const intl = useIntl();
  
  const { data, error, refetch, isPending } = useQuery({
    queryKey: [query],
    queryFn: () => params
      .fetch(query).then(resp => resp.json())
      .then((data: TaskApi.TaskStatusTimelineStatistics[]) => (data ?? []))
      .catch(err => [])
  });

  return { taskTimelineStats: data }
}