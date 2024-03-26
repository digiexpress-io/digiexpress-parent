import { ActorStatus, ChangeType } from './descriptor-types';

export type PrincipalId = string;
export type PrincipalName = string;

export interface Principal {
  id: PrincipalId;
  name: PrincipalName;
  email: string;

  status: ActorStatus;               // users are not deleted; instead, they are disabled
  roles: string[];                   // all role names, irrelevant of inheritance 
  permissions: string[];             // all permission names, irrelevant of inheritance

  directRoles: string[];             // explicitly-given membership in the given role
  directRolePermissions: string[];   // inherited from the role that is directly connected to the principal
  directPermissions: string[];       // explicitly given to this principal only
}

export interface PrincipalCommand {
  targetDate?: string;
  commandType: PrincipalCommandType;
}

export type PrincipalCommandType =
  'CreatePrincipal' |
  'ChangePrincipalRoles' |
  'ChangePrincipalStatus';

export interface PrincipalUpdateCommand<T extends PrincipalCommandType> extends PrincipalCommand {
  id: PrincipalId;
  commandType: T;
}

export interface CreatePrincipal {
  commandType: 'CreatePrincipal';
  name: PrincipalName;
  email: string;
  roles: string[];
  permissions: string[];
}

export interface ChangePrincipalRoles extends PrincipalUpdateCommand<'ChangePrincipalRoles'> {
  roles: string;        // id/name/extId
  changeType: ChangeType;
}

export interface ChangePrincipalStatus extends PrincipalUpdateCommand<'ChangePrincipalStatus'> {
  status: ActorStatus;
}


