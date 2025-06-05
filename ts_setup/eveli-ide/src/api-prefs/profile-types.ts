export namespace PrefsApi {

}

export declare namespace PrefsApi {

  export interface PrefsRestApi {
    currentUserProfile(): Promise<UserProfile>;
    getUserProfileById(id: string): Promise<UserProfile>;
    findAllUserProfiles(): Promise<UserProfile[]>;
    updateUserProfile(profileId: string, commands: UserProfileUpdateCommand<any>[]): Promise<UserProfile>;
    updateUiSettings(commands: UpsertUiSettings): Promise<UserProfile>;
    findUiSettings(settingsId: string): Promise<UiSettings | undefined>;
  }

  export type PreferenceId = string;
  export type DataId = string;

  export interface VisibilityRule {
    dataId: DataId;
    enabled: boolean;
  }

  export interface ConfigRule {
    dataId: DataId;
    value: string;
  }

  export interface Preference {
    id: PreferenceId;
    fields: readonly DataId[]; //used for visibility and sorting as definition for all the field names on top of what constrains can be added
    
    visibility: readonly VisibilityRule[];
    config: readonly ConfigRule[];

    getVisibility(dataId: DataId): VisibilityRule;
    getConfig(dataId: DataId): ConfigRule | undefined;
  }

  export type NotificationType = string;

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


  export interface UiSettingsForConfig {
    dataId: string;
    value: string;
  }

  export interface UiSettings {
    id: string | undefined;
    settingsId: string;
    userId: string;
    visibility: UiSettingForVisibility[];
    config: UiSettingsForConfig[];
  }

  export interface UserProfile {
    id: string,
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
    userId?: string;
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
}