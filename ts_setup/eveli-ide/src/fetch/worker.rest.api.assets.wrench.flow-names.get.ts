import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { useQuery } from '@tanstack/react-query'

export const Hook = createFileFetch('worker/rest/api/assets/wrench/flow-names.GET')({
  hook
}) 

function hook(props: {}): { flows: string[] | undefined } {
  const params = Hook.useParams();
  const { url } = params;
  const query = url({});
  
  const { data, error, refetch, isPending } = useQuery({
    queryKey: [query],
    queryFn: () => params
      .fetch(query).then(resp => resp.json())
      .then((data: string[]) => (data ?? []).sort()),
  });

  return { flows: data }
}