import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { useIntl } from 'react-intl';
import { useQuery } from '@tanstack/react-query'
import { ROLE_AUTHORIZED, IamApi } from '@/burger';

export const Hook = createFileFetch('$org/groupsList.GET')({
  hook
}) 


function hook(props: {}): { groups: IamApi.UserGroup[] } {
  const intl = useIntl();
  const params = Hook.useParams();
  const { url } = params;
  const query = url({ org: '' }).substring(1);

  const { data, error, refetch, isPending } = useQuery({
    staleTime: 15000,
    queryKey: [query],
    queryFn: () => params.fetch(query)
      .then(resp => resp.json())
      .then((data: IamApi.Group[]) => data ?? [])
      .then(data => {
        const result: IamApi.UserGroup[] = data.map(response => {
          return {
            id: response.name,
            groupName: response.description
          }
        });
        result.push({ id: ROLE_AUTHORIZED, groupName: intl.formatMessage({ id: 'task.role.assignedAllUsers' }) });
        return result;
      }),
  });

  return { groups: data ?? [] }
}