import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { useQuery } from '@tanstack/react-query'

import { PublicationApi } from '@/burger';

export const Hook = createFileFetch('worker/rest/api/assets/publications.GET')({
  hook
}) 

function hook(props: {}): { assetReleases: PublicationApi.Publication[] | undefined, refreshAssetReleases: typeof refetch, isLoading: boolean } {
  const params = Hook.useParams();
  const { url } = params;
  
  const query = url({})
  const { data, error, refetch, isPending } = useQuery({
    queryKey: [query],
    queryFn: () => params.fetch(query).then(resp => resp.json()).then((data: PublicationApi.Publication[]) => data),
  });

  return {
    assetReleases: data,
    isLoading: isPending,
    refreshAssetReleases: refetch
  }
}