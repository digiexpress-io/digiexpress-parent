import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { useQuery } from '@tanstack/react-query'

import { Publication } from '../frontdesk/types/Publication';

export const Hook = createFileFetch('worker/rest/api/assets/publications.GET')({
  hook
}) 

function hook(props: {}): { assetReleases: Publication[] | undefined, refreshAssetReleases: typeof refetch, isLoading: boolean } {
  const params = Hook.useParams();
  const { url } = params;
  
  const query = url({})
  const { data, error, refetch, isPending } = useQuery({
    queryKey: [query],
    queryFn: () => params.fetch(query).then(resp => resp.json()).then((data: Publication[]) => data),
  });

  return {
    assetReleases: data,
    isLoading: isPending,
    refreshAssetReleases: refetch
  }
}