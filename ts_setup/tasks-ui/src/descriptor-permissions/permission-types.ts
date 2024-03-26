import { ActorStatus } from './descriptor-types';

export type PermissionId = string;
export type PermissionName = string;

export interface Permission {
  id: PermissionId;
  name: PermissionName;
  description: string;
  status: ActorStatus
}

export interface PermissionCommand {
  targetDate: string | undefined;
  commandType: PermissionCommandType;
}

export type PermissionCommandType =
  'CreatePermission' |
  'ChangePermissionName' |
  'ChangePermissionDescription' |
  'ChangePermissionStatus';

export interface PermissionUpdateCommand<T extends PermissionCommandType> extends PermissionCommand {
  id: PermissionId;
  commandType: T;
}

export interface CreatePermission {
  commandType: 'CreatePermission';
  name: PermissionName;
  description: string;
  roles: string[];
}

export interface ChangePermissionName extends PermissionUpdateCommand<'ChangePermissionName'> {
  name: PermissionName;
}

export interface ChangePermissionDescription extends PermissionUpdateCommand<'ChangePermissionDescription'> {
  description: string;
}

export interface ChangePermissionStatus extends PermissionUpdateCommand<'ChangePermissionStatus'> {
  status: ActorStatus;
}

