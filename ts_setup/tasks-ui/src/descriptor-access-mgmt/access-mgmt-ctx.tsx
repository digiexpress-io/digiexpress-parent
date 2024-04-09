import React from 'react';

import { Backend } from 'client';
import { UserProfileAndOrg } from './profile-types';
import { Principal, Role, RoleId, PrincipalId, Permission } from './access-mgmt-types';
import { UserSearchResult, RoleSearchResult } from './access-mgmt-ctx-state';

export type { UserSearchResult, RoleSearchResult };
export interface AmContextType {
  userId: PrincipalId
  iam: Principal;
  profile: UserProfileAndOrg;
  roles: Role[];
  principals: Principal[];
  permissions: Permission[];
  
  reload: () => Promise<void>;

  getPermission(idOrNameOrExternalId: string): Permission;
  getPrincipal(idOrNameOrExternalId: string): Principal;
  getRole(idOrNameOrExternalId: string): Role;
  findTaskUsers(searchFor: string, checkedUsers: PrincipalId[]): UserSearchResult[];
  findTaskRoles(searchFor: string, checkedRoles: RoleId[]): RoleSearchResult[];
}

export const AmContext = React.createContext<AmContextType>({} as AmContextType);

export const useAm = (): AmContextType => {
  return React.useContext(AmContext);
}

const init: OrgState = new OrgMutatorBuilderImpl({
  iam: { displayName: "", userId: "", userRoles: [], avatar: '' },
  org: { roles: {}, users: {} }
});

export const AmProvider: React.FC<{ children: React.ReactNode, backend: Backend, profile: UserProfileAndOrg }> = ({ children, backend, profile }) => {
  const [state, setState] = React.useState<OrgState>(init);
  const setter: OrgDispatch = React.useCallback((mutator: OrgMutator) => setState(mutator), [setState]);

  const contextValue: AmContextType = React.useMemo(() => {
    return { state, setState: setter, profile };
  }, [state, setter, profile]);


  return (<AmContext.Provider value={contextValue}>{children}</AmContext.Provider>);
}


export const useTaskAssignees = (row: { assignees: PrincipalId[] }) : {
  searchString: string;
  setSearchString: React.Dispatch<React.SetStateAction<string>>;
  searchResults: UserSearchResult[];
} => {
  const org = useOrg();
  const { assignees } = row;
  const [searchString, setSearchString] = React.useState<string>('');
  const searchResults = React.useMemo(() => org.state.findUsers(searchString, assignees), [assignees, searchString, org]);
  return { searchString, setSearchString, searchResults };
}


export const useTaskRoles = (row: { roles: RoleId[] }) : {
  searchString: string;
  setSearchString: React.Dispatch<React.SetStateAction<string>>;
  searchResults: RoleSearchResult[];
} => {
  const org = useOrg();
  const [searchString, setSearchString] = React.useState<string>('');
  const searchResults = React.useMemo(() => org.state.findRoles(searchString, row.roles), [row, searchString, org]);
  return { searchString, setSearchString, searchResults };
}


