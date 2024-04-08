import { ActorStatus, ChangeType } from './types';

export type PermissionId = string;
export type PermissionName = string;
export type RoleId = string;
export type RoleName = string;
export type PrincipalId = string;
export type PrincipalName = string;

//-------------------------PERMISSION-----------------------------

export interface Permission {
  id: PermissionId;
  name: string;
  description: string;
  status: ActorStatus;
}

export interface PermissionCommand {
  comment: string;
}

export type PermissionCommandType =
  'CREATE_PERMISSION' |
  'CHANGE_PERMISSION_NAME' |
  'CHANGE_PERMISSION_DESCRIPTION' |
  'CHANGE_PERMISSION_STATUS';

export interface PermissionUpdateCommand extends PermissionCommand { }

export interface CreatePermission {
  name: string;
  comment: string;
  description: string;
  commandType: 'CREATE_PERMISSION';
  roles: string[];
}

export interface ChangePermissionName extends PermissionUpdateCommand {
  id: PermissionId;
  name: string;
  commandType: 'CHANGE_PERMISSION_NAME';
}

export interface ChangePermissionDescription extends PermissionUpdateCommand {
  id: PermissionId;
  description: string;
  commandType: 'CHANGE_PERMISSION_DESCRIPTION'
}

export interface ChangePermissionStatus extends PermissionUpdateCommand {
  id: PermissionId;
  status: ActorStatus;
  commandType: 'CHANGE_PERMISSION_STATUS'
}

export interface PermissionPagination {
  page: number; //starts from 1
  total: { pages: number, records: number };
  records: Permission[];
}

//-------------------------ROLE-----------------------------

export interface Role {
  id: RoleId;
  parentId: RoleId | undefined;
  name: RoleName;
  description: string;

  status: ActorStatus;

  permissions: string[];  // permission names
  principals: string[];   // user names
}

export interface RoleCommand {
  comment: string;
}

export interface RoleUpdateCommand extends RoleCommand { }

export type RoleCommandType =
  'CREATE_ROLE' |
  'CHANGE_ROLE_NAME' |
  'CHANGE_ROLE_DESCRIPTION' |
  'CHANGE_ROLE_STATUS' |
  'CHANGE_ROLE_PERMISSIONS';

export interface CreateRole {
  commandType: 'CREATE_ROLE';
  name: RoleName;
  description: string;
  permissions: string[];
  comment: string;
  parentId: string | undefined;
}

export interface ChangeRoleName extends RoleUpdateCommand {
  id: RoleId;
  name: RoleName;
  commandType: 'CHANGE_ROLE_NAME';
}

export interface ChangeRoleDescription extends RoleUpdateCommand {
  id: RoleId;
  description: string;
  commandType: 'CHANGE_ROLE_DESCRIPTION';
}

export interface ChangeRoleStatus extends RoleUpdateCommand {
  id: RoleId;
  status: ActorStatus;
  commandType: 'CHANGE_ROLE_STATUS';
}

export interface ChangeRolePermissions extends RoleUpdateCommand {
  id: RoleId;
  permissions: string[];
  changeType: ChangeType;
  commandType: 'CHANGE_ROLE_PERMISSIONS';
}

//-------------------------PRINCIPAL-----------------------------

export interface Principal {
  id: PrincipalId;
  name: PrincipalName;
  email: string;

  status: ActorStatus;               // users are not deleted; instead, they are disabled
  roles: string[];                   // all role names, irrelevant of inheritance 
  permissions: string[];             // all permission names, irrelevant of inheritance

  directRoles: string[];             // explicitly-given membership in the given role
  directPermissions: string[];       // explicitly given to this principal only

  // directRolePermissions: string[];   // inherited from the role that is directly connected to the principal


}

export interface PrincipalCommand {
  comment: string;
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
  comment: string;
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

export interface AccessMgmtStore {
  findAllPermissions(): Promise<Permission[]>;
  getPermission(id: PermissionId): Promise<Permission>;
  createPermission(command: CreatePermission): Promise<Permission>;
  updatePermission(id: PermissionId, commands: PermissionUpdateCommand[]): Promise<Permission>;

  findAllRoles(): Promise<Role[]>;
  getRole(id: RoleId): Promise<Role>;
  createRole(command: CreateRole): Promise<Role>;
  updateRole(id: RoleId, commands: RoleUpdateCommand[]): Promise<Permission>;
}

