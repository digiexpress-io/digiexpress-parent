import { createFileFetch } from '@dxs-ts/eveli-fetch';
import composerVersion from '../version';
import { useQuery } from '@tanstack/react-query';


export const Hook = createFileFetch('worker/rest/api/version.GET')({
  hook
}) 

export interface VersionEntity {
  version: string;
  built: string;
}


function hook(props: {}): {
  frontend: VersionEntity,
  backend: VersionEntity
} | undefined {
  const params = Hook.useParams();
  const { url, method } = params;
  const query = url({ });

  const { data, error, refetch, isPending } = useQuery({
    staleTime: 15000,
    queryKey: [query],
    queryFn: () => params.fetch(query)
      .then(resp => resp.json())
      .then((data: VersionEntity) => (
        {
          frontend:  { version: composerVersion.tag, built: composerVersion.built },
          backend: data
        })),
  });

  return data;

}