import { Org, User, UserId, Role, RoleId } from 'taskclient';

export interface OrgContextType {
  setState: OrgDispatch;
  loading: boolean;
  state: OrgState,
}

export type OrgMutator = (prev: OrgState) => OrgState;
export type OrgDispatch = (mutator: OrgMutator) => void;

export interface UserSearchResult {
  checked: boolean,
  avatar: { twoletters: string, value: UserId },
  user: User
}

export interface RoleSearchResult {
  checked: boolean,
  avatar: { twoletters: string, value: RoleId },
  role: Role
}

export interface OrgState {
  org: Org;
  iam: User;

  withOrg(value: Org): OrgState;
  withIam(value: User): OrgState;

  findUsers(searchFor: string, checkedUsers: UserId[]): UserSearchResult[];
  findRoles(searchFor: string, checkedRoles: RoleId[]): RoleSearchResult[];
}