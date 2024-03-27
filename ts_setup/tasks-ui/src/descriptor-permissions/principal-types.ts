import { ActorStatus, ChangeType } from './types';

export type PrincipalId = string;
export type PrincipalName = string;

export interface Principal {
  id: PrincipalId;
  name: PrincipalName;
  email: string;

  status: ActorStatus;               // users are not deleted; instead, they are disabled
  roles: string[];                   // all role names, irrelevant of inheritance 

  /*
  permissions: string[];             // all permission names, irrelevant of inheritance
  directRoles: string[];             // explicitly-given membership in the given role
  directRolePermissions: string[];   // inherited from the role that is directly connected to the principal
  directPermissions: string[];       // explicitly given to this principal only

  */
}

export interface PrincipalCommand {
  targetDate?: string;
  commandType: PrincipalCommandType;
}

export type PrincipalCommandType =
  'CREATE_PRINCIPAL' |
  'CHANGE_PRINCIPAL_ROLES' |
  'CHANGE_PRINCIPAL_STATUS';

export interface PrincipalUpdateCommand extends PrincipalCommand { }

export interface CreatePrincipal {
  commandType: 'CREATE_PRINCIPAL';
  name: PrincipalName;
  email: string;
  roles: string[];
  permissions: string[];
}

export interface ChangePrincipalRoles extends PrincipalUpdateCommand {
  id: PrincipalId;
  roles: string;        // id/name/extId
  changeType: ChangeType;
  commandType: 'CHANGE_PRINCIPAL_ROLES';
}

export interface ChangePrincipalStatus extends PrincipalUpdateCommand {
  id: PrincipalId;
  status: ActorStatus;
  commandType: 'CHANGE_PRINCIPAL_STATUS';
}


