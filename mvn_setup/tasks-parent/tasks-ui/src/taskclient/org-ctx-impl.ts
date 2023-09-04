import {
  UserId, User, Org
} from './client-types';

import {
  OrgState, UserSearchResult
} from './org-ctx-types';



interface ExtendedInit {
  org: Org; 
  iam: User;
}

class OrgMutatorBuilderImpl implements OrgState {
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
  
  clone(): ExtendedInit {
    const init = this;
    return {
      org: init.org,
      iam: init.iam
    }
  }
}

export { OrgMutatorBuilderImpl };
export type { };
