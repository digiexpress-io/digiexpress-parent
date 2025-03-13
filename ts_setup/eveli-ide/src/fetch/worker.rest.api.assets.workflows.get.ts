import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { Workflow } from '../frontdesk/types/Workflow';
import { useQuery } from '@tanstack/react-query';

export const Hook = createFileFetch('worker/rest/api/assets/workflows.GET')({
  hook
}) 

function hook(props: {}): {
  workflows: Workflow[] | undefined, refreshWorkflows: typeof refetch
} {
  const params = Hook.useParams();
  const { url, method } = params;


  const query = url({});
  
  const { data, error, refetch, isPending } = useQuery({
    queryKey: [query],
    queryFn: () => params
      .fetch(query).then(resp => resp.json())
      .then((data: Workflow[]) => data),
  });

  return { workflows: data, refreshWorkflows: refetch }
}