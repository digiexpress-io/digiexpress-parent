import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { useQuery } from '@tanstack/react-query';
import { PublicationApi  } from '@/api-publications';

export const Hook = createFileFetch('worker/rest/api/assets/workflows.GET')({
  hook
}) 

function hook(props: {}): {
  workflows: PublicationApi.AssetService[] | undefined, refreshWorkflows: typeof refetch
} {
  const params = Hook.useParams();
  const { url, method } = params;


  const query = url({});
  
  const { data, error, refetch, isPending } = useQuery({
    queryKey: [query],
    queryFn: () => params
      .fetch(query).then(resp => resp.json())
      .then((data: PublicationApi.AssetService[]) => data),
  });

  return { workflows: data, refreshWorkflows: refetch }
}