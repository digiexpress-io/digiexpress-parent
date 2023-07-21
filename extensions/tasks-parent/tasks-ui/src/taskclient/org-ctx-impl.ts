import {
  User, Org
} from './client-types';

import {
  OrgMutatorBuilder, OrgState
} from './org-ctx-types';



interface ExtendedInit extends OrgState {

}

class OrgMutatorBuilderImpl implements OrgMutatorBuilder {
  private _org: Org; 
  private _iam: User;

  constructor(init: ExtendedInit) {
    this._org = init.org;
    this._iam = init.iam;
  }
  get org(): Org { return this._org };
  get iam(): User { return this._iam };

  withIam(value: User): OrgMutatorBuilder {
    return new OrgMutatorBuilderImpl({ ...this.clone(), iam: value });
  }
  withOrg(value: Org): OrgMutatorBuilder {
    return new OrgMutatorBuilderImpl({ ...this.clone(), org: value });
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
