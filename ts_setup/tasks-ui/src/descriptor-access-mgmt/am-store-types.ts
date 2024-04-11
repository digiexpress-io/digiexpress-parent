import {
  Permission, PermissionId, Role, RoleId,
  PermissionUpdateCommand, RoleUpdateCommand,
  CreatePermission, CreateRole,
} from "./permission-types";

import {
  UserProfileAndOrg, UserProfileUpdateCommand, UserProfile
} from "./profile-types";




export interface AmStore {
  findAllPermissions(): Promise<Permission[]>;
  getPermission(id: PermissionId): Promise<Permission>;
  createPermission(command: CreatePermission): Promise<Permission>;
  updatePermission(id: PermissionId, commands: PermissionUpdateCommand[]): Promise<Permission>;

  findAllRoles(): Promise<Role[]>;
  getRole(id: RoleId): Promise<Role>;
  createRole(command: CreateRole): Promise<Role>;
  updateRole(id: RoleId, commands: RoleUpdateCommand[]): Promise<Role>;

  currentUserProfile(): Promise<UserProfileAndOrg>;
  getUserProfileById(id: string): Promise<UserProfile>;
  findAllUserProfiles(): Promise<UserProfile[]>;
  updateUserProfile(profileId: string, commands: UserProfileUpdateCommand<any>[]): Promise<UserProfile>;
}