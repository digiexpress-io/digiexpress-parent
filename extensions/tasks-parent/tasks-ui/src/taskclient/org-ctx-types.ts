import { Org, User } from './client-types'; 

export interface OrgContextType {
  setState: OrgDispatch;
  loading: boolean;
  state: OrgState,
}

export type OrgMutator = (prev: OrgMutatorBuilder) => OrgMutatorBuilder;
export type OrgDispatch = (mutator: OrgMutator) => void;
export interface OrgState { 
  org: Org; 
  iam: User; 
}

export interface OrgMutatorBuilder extends OrgState {
  withOrg(value: Org): OrgMutatorBuilder;
  withIam(value: User): OrgMutatorBuilder;
}
