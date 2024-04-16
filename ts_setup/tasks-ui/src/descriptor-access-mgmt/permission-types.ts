export type ActorStatus = 'IN_FORCE' | 'DISABLED'; // Actors cannot be deleted -- instead, they are "disabled"
export type ChangeType = 'ADD' | 'REMOVE' | 'DISABLE' | 'SET_ALL';
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

  roles: string[];
  principals: string[];
}

export interface PermissionCommand {
  comment: string;
}

export type PermissionCommandType =
  'CREATE_PERMISSION' |
  'CHANGE_PERMISSION_NAME' |
  'CHANGE_PERMISSION_DESCRIPTION' |
  'CHANGE_PERMISSION_STATUS' |
  'CHANGE_PERMISSION_ROLES' |
  'CHANGE_PERMISSION_PRINCIPALS';

export interface PermissionUpdateCommand extends PermissionCommand { }

export interface CreatePermission {
  name: string;
  comment: string;
  description: string;
  commandType: 'CREATE_PERMISSION';
  roles: string[];
  principals: string[];
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

export interface ChangePermissionRoles extends PermissionUpdateCommand {
  id: PermissionId,
  roles: string[];
  changeType: ChangeType;
  commandType: 'CHANGE_PERMISSION_ROLES';
}

export interface ChangePermissionPrincipals extends PermissionUpdateCommand {
  id: PermissionId,
  principals: string[];
  changeType: ChangeType;
  commandType: 'CHANGE_PERMISSION_PRINCIPALS';
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
  'CHANGE_ROLE_PARENT' |
  'CHANGE_ROLE_PERMISSIONS' |
  'CHANGE_ROLE_PRINCIPALS';

export interface CreateRole {
  commandType: 'CREATE_ROLE';
  name: RoleName;
  description: string;
  permissions: string[];
  principals: string[];
  comment: string;
  parentId: RoleId | undefined;
}

export interface ChangeRoleName extends RoleUpdateCommand {
  id: RoleId;
  name: RoleName;
  commandType: 'CHANGE_ROLE_NAME';
}

export interface ChangeRoleParent extends RoleUpdateCommand {
  id: RoleId;
  parentId: RoleId | undefined;
  commandType: 'CHANGE_ROLE_PARENT';
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

export interface ChangeRolePrincipals extends RoleUpdateCommand {
  id: RoleId;
  principals: string[];
  changeType: ChangeType;
  commandType: 'CHANGE_ROLE_PRINCIPALS';
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
  'CHANGE_PRINCIPAL_STATUS' |
  'CHANGE_PRINCIPAL_PERMISSIONS' |
  'CHANGE_PRINCIPAL_ROLES';

export interface PrincipalUpdateCommand extends PrincipalCommand { }

export interface CreatePrincipal {
  commandType: 'CREATE_PRINCIPAL';
  name: PrincipalName;
  email: string;
  roles: string[];
  permissions: string[];
  comment: string;
}

export interface ChangePrincipalStatus extends PrincipalUpdateCommand {
  id: PrincipalId;
  status: ActorStatus;
  commandType: 'CHANGE_PRINCIPAL_STATUS';
}

export interface ChangePrincipalRoles extends PrincipalUpdateCommand {
  id: PrincipalId;
  roles: string[];        // id/name/extId
  changeType: ChangeType;
  commandType: 'CHANGE_PRINCIPAL_ROLES';
}

export interface ChangePrincipalPermissions extends PrincipalUpdateCommand {
  id: PrincipalId;
  permissions: string[];        // id/name/extId
  changeType: ChangeType;
  commandType: 'CHANGE_PRINCIPAL_PERMISSIONS';
}
