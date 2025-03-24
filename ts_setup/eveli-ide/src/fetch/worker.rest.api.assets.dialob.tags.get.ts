import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { useQuery } from '@tanstack/react-query'
import { StencilApi } from '@/burger';;

export const Hook = createFileFetch('worker/rest/api/assets/dialob/tags.GET')({
  hook
}) 

function hook(props: {}): { allTags: StencilApi.DialobTagAsset[] } {
  const params = Hook.useParams();
  const { url } = params;
  const query = url({});
  
  const { data, error, refetch, isPending } = useQuery({
    queryKey: [query],
    queryFn: () => params
      .fetch(query).then(resp => resp.json())
      .then((data: StencilApi.DialobTagAsset[]) => (data ?? []).sort()),
  });

  return { allTags: data ?? [] }
}