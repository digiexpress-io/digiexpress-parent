
import { RoleId } from "./org-types";

export type NotificationType = string;
export type UserId = string;

export interface UserProfileAndOrg {
  user: UserProfile;
  userId: UserId,
  roles: RoleId[];
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

export interface UiSettings {
  id: string | undefined;
  settingsId: string;
  visibility: UiSettingForVisibility[];
  sorting: UiSettingsForSorting[];
}

export interface UserProfile {
  id: UserId,
  created: string;
  updated: string;
  details: UserDetails;
  uiSettings?: UiSettings[] | undefined;
  notificationSettings: NotificationSetting[];
}

export interface UserProfileTransaction {
  id: string;
  commands: UserProfileCommand[];
}

export interface UserProfileCommand {
  id: string;
  userId?: UserId;
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
  'ArchiveUserProfile' | 
  'UpsertUiSettings';

export interface UserProfileUpdateCommand<T extends UserProfileCommandType> extends UserProfileCommand {
  commandType: T;
}

export interface CreateUserProfile extends UserProfileCommand {
  commandType: 'CreateUserProfile';
  details: UserDetails;
  notificationSettings: NotificationSetting[];
}

export interface UpsertUiSettings extends UserProfileUpdateCommand<'UpsertUiSettings'> {
  commandType: 'UpsertUiSettings';
  uiSettings: UiSettings;
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

export interface UserProfileStore {
  findAllUserProfiles(): Promise<UserProfile[]>
  getUserProfileById(id: UserId): Promise<UserProfile>
  updateUserProfile(id: string, commands: UserProfileUpdateCommand<any>[]): Promise<UserProfile>
}


