import { createFileFetch } from '@dxs-ts/envir-fetch';
import { useQuery } from '@tanstack/react-query';

export const Hook = createFileFetch('worker/rest/api/tasks/in-house.GET')({
  hook
})

function hook(props: {}): { inHouseWorkflows: any[] | undefined; refreshInHouseWorkflows: typeof refetch } {
  const params = Hook.useParams();
  const { url, method } = params;

  const query = url({});

  const { data, refetch } = useQuery({
    queryKey: [query],
    queryFn: () => params.fetch(query, { method }).then((resp) => resp.json()),
  });

  return {
    inHouseWorkflows: data,
    refreshInHouseWorkflows: refetch,
  };
}
