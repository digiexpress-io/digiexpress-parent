import { Permission, Principal, PrincipalId, Role, RoleId } from "./permission-types";
import { UserSearchResult, RoleSearchResult, PermissionContainer } from "./permission-container-types";

export class ImmutablePermissionContainer implements PermissionContainer {
  private _permissions: Permission[];
  private _principals: Principal[];
  private _roles: Role[];

  constructor(permissions: Permission[], principals: Principal[], roles: Role[]) {
    this._permissions = permissions;
    this._principals = principals;
    this._roles = roles;
  }
  findTaskUsers(searchFor: string, checkedUsers: PrincipalId[]): UserSearchResult[] {
    const criteria = searchFor.toLowerCase();
    const target = this._principals;

    const result = criteria ?
      target.filter(entry => 
        entry.id.toLowerCase().includes(criteria) ||
        entry.email.toLowerCase().includes(criteria) ||
        entry.name.toLowerCase().includes(criteria) 
      ) :
      target;

    return result.map(user => ({
      checked: checkedUsers.includes(user.id),
      user
    }));
  }

  findTaskRoles(searchFor: string, checkedRoles: RoleId[]): RoleSearchResult[] {
    const criteria = searchFor.toLowerCase();
    const target = Object.values(this._roles);

    const result = criteria ?
      target.filter(entry => 
        entry.id.toLowerCase().includes(criteria) ||
        entry.name.toLowerCase().includes(criteria) ||
        entry.description.toLowerCase().includes(criteria) 
      ) :
      target;

    return result.map(role => ({
      checked: checkedRoles.includes(role.id),
      role
    }));
  }
}
