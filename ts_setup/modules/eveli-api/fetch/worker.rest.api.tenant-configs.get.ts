
import { createFileFetch } from '@dxs-ts/envir-fetch';
import { useQuery } from '@tanstack/react-query'
import { TenantConfig } from '../api-tenant-config';


export const Hook = createFileFetch('worker/rest/api/tenant-configs.GET')({
  hook
})

function hook(props: {}): { 
  tenantConfig: TenantConfig | undefined,
  pending: boolean
}  {
  const params = Hook.useParams();
  const { url } = params;
  const query = url({});

  const { data, isPending } = useQuery({
    queryKey: [query],
    queryFn: () => window
      .fetch(query).then(resp => resp.json())
      .then((data: TenantConfig) => data)
  });
  return { tenantConfig: data, pending: isPending }
}