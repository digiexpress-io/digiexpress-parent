import { Principal, Role, RoleId, PrincipalId } from "./permission-types";

export interface UserSearchResult {
  checked: boolean,
  user: Principal
}

export interface RoleSearchResult {
  checked: boolean,
  role: Role
}
export interface PermissionContainer {
  findTaskUsers(searchFor: string, checkedUsers: PrincipalId[]): UserSearchResult[];
  findTaskRoles(searchFor: string, checkedRoles: RoleId[]): RoleSearchResult[];
}
