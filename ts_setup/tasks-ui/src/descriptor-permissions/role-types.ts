import { ActorStatus, ChangeType } from './types';
import { Permission } from './permission-types';
import { Principal } from './principal-types';

export type RoleId = string;
export type RoleName = string;

export interface Role {
  id: RoleId;
  //parentId: RoleId | undefined; TODO
  name: RoleName;
  description: string;

  status: ActorStatus;

  permissions: Permission[];  // permission names
  principals: Principal[];   // user names
}

export interface RoleCommand {
  targetDate?: string;
  commandType: RoleCommandType;
}

export interface RoleUpdateCommand extends RoleCommand { }

export type RoleCommandType =
  'CREATE_ROLE' |
  'CHANGE_ROLE_NAME' |
  'CHANGE_ROLE_DESCRIPTION' |
  'CHANGE_ROLE_STATUS' |
  'CHANGE_ROLE_PERMISSIONS';

export interface CreateRole {
  commandType: 'CreateRole';
  name: RoleName;
  description: string;
  permissions: string[];
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
