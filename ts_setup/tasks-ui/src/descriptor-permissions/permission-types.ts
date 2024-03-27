import { ActorStatus } from './types';

export type PermissionId = string;
export type PermissionName = string;


export interface Permission {
  id: PermissionId;
  name: string;
  description: string;
  status: ActorStatus;
}

export interface PermissionCommand {
  targetDate: string | undefined;
  commandType: PermissionCommandType;
}

export type PermissionCommandType =
  'CREATE_PERMISSION' |
  'CHANGE_PERMISSION_NAME' |
  'CHANGE_PERMISSION_DESCRIPTION' |
  'CHANGE_PERMISSION_STATUS';

export interface PermissionUpdateCommand extends PermissionCommand { }

export interface CreatePermission {
  commandType: 'CREATE_PERMISSION';
  name: string;
  description: string;
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

export interface PermissionStore {
  findPermissions(): Promise<Permission[]>;
  getPermission(id: PermissionId): Promise<Permission>;
  createPermission(command: CreatePermission): Promise<Permission>;
  updatePermission(id: PermissionId, commands: PermissionUpdateCommand[]): Promise<Permission>;
}

