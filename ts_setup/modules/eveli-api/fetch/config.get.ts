import { createFileFetch } from '@dxs-ts/envir-fetch';
import { useQuery } from '@tanstack/react-query'

import { useIntl } from 'react-intl';
import { Config } from '../api-config';


export const Hook = createFileFetch('config.GET')({
  hook
}) 

function hook(props: {}): { 
  config: Config | undefined,
  pending: boolean
} {

  const params = Hook.useNativeParams();

  const { url } = params;
  const query = url({});
  const intl = useIntl();
  
  const { data, error, refetch, isPending } = useQuery({
    queryKey: [query],
    queryFn: () => window
      .fetch(query).then(resp => resp.json())
      .then((data: Config) => data)
  });

  return { config: data, pending: isPending }
}