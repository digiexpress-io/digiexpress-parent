
import { RoleId } from "./org-types";

export type NotificationType = string;
export type UserId = string;

/*
export interface UserProfile {
  name: string,
  userId: UserId,
  roles: RoleId[],
  today: Date,
*/

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

export interface UserProfile {
  created: Date;
  updated: Date;
  details: UserDetails;
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
  'ArchiveUserProfile';

export interface UserProfileUpdateCommand<T extends UserProfileCommandType> extends UserProfileCommand {
  commandType: T;
}

export interface CreateUserProfile extends UserProfileCommand {
  commandType: 'CreateUserProfile';
  details: UserDetails;
  notificationSettings: NotificationSetting[];
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


