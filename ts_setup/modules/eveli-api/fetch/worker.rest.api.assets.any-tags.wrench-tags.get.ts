import { createFileFetch } from '@dxs-ts/envir-fetch';
import { useQuery } from '@tanstack/react-query'

import { PublicationApi } from '../api-publications'

export const Hook = createFileFetch('worker/rest/api/assets/any-tags/wrench-tags.GET')({
  hook
}) 

function hook(props: {}): { wrenchTags: PublicationApi.AssetTag[] | undefined } {
  const params = Hook.useParams();
  const { url } = params;
  const query = url({});
  
  const { data, error, refetch, isPending } = useQuery({
    queryKey: [query],
    queryFn: () => params.fetch(query).then(resp => resp.json()),
  });

  return { wrenchTags: data }
}