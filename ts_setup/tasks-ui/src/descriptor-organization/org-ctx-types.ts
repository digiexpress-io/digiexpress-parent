import { Org, User, UserId, Role, RoleId } from 'client';
import { UserProfileAndOrg } from 'descriptor-user-profile';

export interface OrgContextType {
  setState: OrgDispatch;
  loading: boolean;
  state: OrgState,
  profile: UserProfileAndOrg;
}

export type OrgMutator = (prev: OrgState) => OrgState;
export type OrgDispatch = (mutator: OrgMutator) => void;

export interface UserSearchResult {
  checked: boolean,
  user: User
}

export interface RoleSearchResult {
  checked: boolean,
  role: Role
}

export interface OrgState {
  org: Org;
  iam: User;

  withOrg(value: Org): OrgState;
  withIam(value: User): OrgState;

  findProjectUsers(searchFor: string, checkedUsers: UserId[]): UserSearchResult[];
  findUsers(searchFor: string, checkedUsers: UserId[]): UserSearchResult[];
  findRoles(searchFor: string, checkedRoles: RoleId[]): RoleSearchResult[];
}