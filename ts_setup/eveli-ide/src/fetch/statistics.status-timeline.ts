import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { useQuery } from '@tanstack/react-query'


import { useIntl } from 'react-intl';
import { TaskStatusTimelineStatistics } from 'frontdesk/types/TaskStatistics';


export const Hook = createFileFetch('statistics/status-timeline.GET')({
  hook
}) 

function hook(props: {}): { taskTimelineStats: TaskStatusTimelineStatistics[] | undefined } {

  const params = Hook.useParams();
  const { url } = params;
  const query = url({});
  const intl = useIntl();
  
  const { data, error, refetch, isPending } = useQuery({
    queryKey: [query],
    queryFn: () => params
      .fetch(query).then(resp => resp.json())
      .then((data: TaskStatusTimelineStatistics[]) => (data ?? []))
      .catch(err => [])
  });

  return { taskTimelineStats: data }
}