import { createFileFetch } from '@dxs-ts/eveli-fetch';

import { ROLE_AUTHORIZED, IamApi } from '@/api-iam';


export const Hook = createFileFetch('$org/groupMembership.GET')({
  hook
}) 


function hook(props: {}) {
  const params = Hook.useParams();
  const { url } = params;

  return {
    getUsers: async (groupName: string[]): Promise<IamApi.GroupMember[]> => {

      if (!groupName || groupName.length === 0) {
        return [];
      }
      const filteredGroups = groupName.filter(name => name !== ROLE_AUTHORIZED).join(',');
      if (!filteredGroups) {
        return [];
      }
      const restApi = url({ org: '' }).substring(1) + `/?groupName=${filteredGroups}`;
      return params
        .fetch(restApi)
        .then(response => response.json())
        .catch(e => {
          console.warn(`Disabled rest api: ${restApi}`);
          return [];
        });
    }
  }
}