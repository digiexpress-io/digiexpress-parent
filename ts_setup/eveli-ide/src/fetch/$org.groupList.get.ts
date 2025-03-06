import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { useIntl } from 'react-intl';
import { useQuery } from '@tanstack/react-query'
import { Group as OrgGroup } from '../frontdesk/types/Group';
import { ROLE_AUTHORIZED } from '@/burger';
import { UserGroup } from '../frontdesk/types/UserGroup';

export const Hook = createFileFetch('$org/groupList.GET')({
  hook
}) 

function hook(props: {}): { groups: UserGroup[] } {
  const intl = useIntl();
  const params = Hook.useParams();
  const { url } = params;
  const query = url({ org: '' }).substring(1);

  const { data, error, refetch, isPending } = useQuery({
    staleTime: 15000,
    queryKey: [query],
    queryFn: () => params.fetch(query)
      .then(resp => resp.json())
      .then((data: OrgGroup[]) => data ?? [])
      .then(data => {
        const result: UserGroup[] = data.map(response => {
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