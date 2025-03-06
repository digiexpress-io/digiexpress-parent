import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { useQuery } from '@tanstack/react-query'

import { DialobFormEntry } from '../frontdesk/types';

export const Hook = createFileFetch('worker/rest/api/assets/dialob.GET')({
  hook
}) 

function hook(props: {}): { dialobForms: DialobFormEntry[] | undefined, refresh: typeof refetch} {
  const params = Hook.useParams();
  const { url } = params;
  const query = url({});
  
  const { data, error, refetch, isPending } = useQuery({
    queryKey: [query],
    queryFn: () => params
      .fetch(query).then(resp => resp.json())
      .then((data: DialobFormEntry[]) => (data ?? []).sort()),
  });

  return { dialobForms: data, refresh: refetch }
}