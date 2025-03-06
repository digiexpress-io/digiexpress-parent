import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { GroupMember } from '../frontdesk/types/GroupMember';
import { ROLE_AUTHORIZED } from '@/burger';


export const Hook = createFileFetch('$org/groupMembership.GET')({
  hook
}) 


function hook(props: {}) {
  const params = Hook.useParams();
  const { url } = params;

  return {
    getUsers: async (groupName: string[]): Promise<GroupMember[]> => {

      if (!groupName || groupName.length === 0) {
        return [];
      }
      const filteredGroups = groupName.filter(name => name !== ROLE_AUTHORIZED).join(',');
      if (!filteredGroups) {
        return [];
      }
      
      return params
        .fetch(url({ org: '' }).substring(1)+ `/?groupName=${filteredGroups}`)
        .then(response => response.json());
    }
  }
}