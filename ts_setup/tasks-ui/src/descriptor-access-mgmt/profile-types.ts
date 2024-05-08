import { TenantConfig } from './tenant-types';
import { Permission, Principal, PrincipalId, Role } from './permission-types';


export type NotificationType = string;

export interface UserProfileAndOrg {
  userId: string; 
  am: {  // current user related AM
    principal: Principal;
    permissions: string[];
    roles: string[];
  };
  all: {
    permissions: Record<string, Permission>;
    principals: Record<string, Principal>;
    roles: Record<string, Role>;
  }
  tenant: TenantConfig;
  user: UserProfile;
  today: Date;
}

export interface UserDetails {
  username: string;
  firstName: string;
  lastName: string;
  email: string;
}

export interface NotificationSetting {
  enabled: boolean,
  type: NotificationType
}


export interface UiSettingForVisibility {
  dataId: string;
  enabled: boolean;
}

export interface UiSettingsForSorting {
  dataId: string;
  direction: 'asc' | 'desc';
}


export interface UiSettingsForConfig {
  dataId: string;
  value: string;
}

export interface UiSettings {
  id: string | undefined;
  settingsId: string;
  userId: string;
  visibility: UiSettingForVisibility[];
  sorting: UiSettingsForSorting[];
  config: UiSettingsForConfig[];
}

export interface UserProfile {
  id: PrincipalId,
  created: string;
  updated: string;
  details: UserDetails;
  notificationSettings: NotificationSetting[];
}

export interface UserProfileTransaction {
  id: string;
  commands: UserProfileCommand[];
}

export interface UserProfileCommand {
  id: string;
  userId?: PrincipalId;
  targetDate?: Date;
  commandType: UserProfileCommandType;
}

type UserProfileCommandType =
  'CreateUserProfile' |
  'UpsertUserProfile' |
  'ChangeUserDetailsFirstName' |
  'ChangeUserDetailsLastName' |
  'ChangeUserDetailsEmail' |
  'ChangeNotificationSetting' |
  'ArchiveUserProfile';

export interface UserProfileUpdateCommand<T extends UserProfileCommandType> extends UserProfileCommand {
  commandType: T;
}

export interface CreateUserProfile extends UserProfileCommand {
  commandType: 'CreateUserProfile';
  details: UserDetails;
  notificationSettings: NotificationSetting[];
}

export interface UpsertUiSettings{
  commandType: 'UpsertUiSettings';
  userId: string;
  settingsId: string;
  visibility: UiSettingForVisibility[];
  sorting: UiSettingsForSorting[];
  config: UiSettingsForConfig[];
}

export interface UpsertUserProfile extends UserProfileUpdateCommand<'UpsertUserProfile'> {
  commandType: 'UpsertUserProfile';
  details: UserDetails;
  notificationSettings: NotificationSetting[];
}

export interface ChangeUserDetailsFirstName extends UserProfileUpdateCommand<'ChangeUserDetailsFirstName'> {
  commandType: 'ChangeUserDetailsFirstName';
  firstName: string;
}

export interface ChangeUserDetailsLastName extends UserProfileUpdateCommand<'ChangeUserDetailsLastName'> {
  commandType: 'ChangeUserDetailsLastName';
  lastName: string;
}

export interface ChangeUserDetailsEmail extends UserProfileUpdateCommand<'ChangeUserDetailsEmail'> {
  commandType: 'ChangeUserDetailsEmail';
  email: string;
}

export interface ChangeNotificationSetting extends UserProfileUpdateCommand<'ChangeNotificationSetting'> {
  commandType: 'ChangeNotificationSetting';
  type: NotificationType;
  enabled: boolean;
}

export interface ArchiveUserProfile extends UserProfileUpdateCommand<'ArchiveUserProfile'> {
  commandType: 'ArchiveUserProfile';
}

export interface UserProfileDescriptor {
  id: PrincipalId,
  email: string | undefined,
  displayName: string,
  entry: UserProfile,
  created: Date,
  updated: Date,
  notificationSettings: NotificationSetting[];
}