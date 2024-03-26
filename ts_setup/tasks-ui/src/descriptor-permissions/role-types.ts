import { ActorStatus, ChangeType } from './descriptor-types';

export type RoleId = string;
export type RoleName = string;

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
  targetDate?: string;
  commandType: RoleCommandType;
}

export interface RoleUpdateCommand<T extends RoleCommandType> extends RoleCommand {
  id: RoleId;
  commandType: T;
}

export type RoleCommandType =
  'CreateRole' |
  'ChangeRoleName' |
  'ChangeRoleDescription' |
  'ChangeRoleStatus' |
  'ChangeRolePermissions';

export interface CreateRole {
  commandType: 'CreateRole';
  name: RoleName;
  description: string;
  permissions: string[];
}

export interface ChangeRoleName extends RoleUpdateCommand<'ChangeRoleName'> {
  name: RoleName;
}

export interface ChangeRoleDescription extends RoleUpdateCommand<'ChangeRoleDescription'> {
  description: string;
}

export interface ChangeRoleStatus extends RoleUpdateCommand<'ChangeRoleStatus'> {
  status: ActorStatus;
}

export interface ChangeRolePermissions extends RoleUpdateCommand<'ChangeRolePermissions'> {
  permissions: string[];
  changeType: ChangeType;
}
