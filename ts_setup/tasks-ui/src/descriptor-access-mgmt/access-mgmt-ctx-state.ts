
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

  findUsers(searchFor: string, checkedUsers: UserId[]): UserSearchResult[];
  findRoles(searchFor: string, checkedRoles: RoleId[]): RoleSearchResult[];
}

export interface ExtendedInit {
  org: Org;
  iam: User;
}

export class OrgMutatorBuilderImpl implements OrgState {
  private _org: Org;
  private _iam: User;

  constructor(init: ExtendedInit) {
    this._org = init.org;
    this._iam = init.iam;
  }
  get org(): Org { return this._org };
  get iam(): User { return this._iam };

  withIam(value: User): OrgState {
    return new OrgMutatorBuilderImpl({ ...this.clone(), iam: value });
  }
  withOrg(value: Org): OrgState {
    return new OrgMutatorBuilderImpl({ ...this.clone(), org: value });
  }
  findProjectUsers(searchFor: string, checkedUsers: UserId[]): UserSearchResult[] {
    const criteria = searchFor.toLowerCase();
    const target = Object.values(this._org.users);

    const result = criteria ?
      target.filter(entry => entry.displayName.toLowerCase().includes(criteria)) :
      target;

    return result.map(user => ({
      checked: checkedUsers.includes(user.userId),
      avatar: { twoletters: user.avatar, value: user.userId },
      user
    }));
  }

  findUsers(searchFor: string, checkedUsers: UserId[]): UserSearchResult[] {
    const criteria = searchFor.toLowerCase();
    const target = Object.values(this._org.users);

    const result = criteria ?
      target.filter(entry => entry.displayName.toLowerCase().includes(criteria)) :
      target;

    return result.map(user => ({
      checked: checkedUsers.includes(user.userId),
      avatar: { twoletters: user.avatar, value: user.userId },
      user
    }));
  }

  findRoles(searchFor: string, checkedRoles: RoleId[]): RoleSearchResult[] {
    const criteria = searchFor.toLowerCase();
    const target = Object.values(this._org.roles);

    const result = criteria ?
      target.filter(entry => entry.displayName.toLowerCase().includes(criteria)) :
      target;

    return result.map(role => ({
      checked: checkedRoles.includes(role.roleId),
      avatar: { twoletters: role.avatar, value: role.roleId },
      role
    }));
  }

  clone(): ExtendedInit {
    const init = this;
    return {
      org: init.org,
      iam: init.iam
    }
  }
}
