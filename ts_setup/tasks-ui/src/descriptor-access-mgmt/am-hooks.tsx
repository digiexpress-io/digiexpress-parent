import React from 'react';

import { PrincipalId, RoleId, Principal, Role} from './permission-types';
import { UserSearchResult, RoleSearchResult } from './permission-container-types';
import { useAm } from './AccessMgmtContext';

export const useTaskAssignees = (row: { assignees: PrincipalId[] }) : {
  searchString: string;
  setSearchString: React.Dispatch<React.SetStateAction<string>>;
  searchResults: UserSearchResult[];
} => {
  const org = useAm();
  const { assignees } = row;
  const [searchString, setSearchString] = React.useState<string>('');
  const searchResults = React.useMemo(() => org.findTaskUsers(searchString, assignees), [assignees, searchString, org]);
  return { searchString, setSearchString, searchResults };
}


export const useTaskRoles = (row: { roles: RoleId[] }) : {
  searchString: string;
  setSearchString: React.Dispatch<React.SetStateAction<string>>;
  searchResults: RoleSearchResult[];
} => {
  const org = useAm();
  const [searchString, setSearchString] = React.useState<string>('');
  const searchResults = React.useMemo(() => org.findTaskRoles(searchString, row.roles), [row, searchString, org]);
  return { searchString, setSearchString, searchResults };
}


export const usePrincipalDisplayName = (principal: Principal): string => {

  return principal.name;
}

export const useRoleDisplayName = (role: Role): string => {
  return role.name;
}