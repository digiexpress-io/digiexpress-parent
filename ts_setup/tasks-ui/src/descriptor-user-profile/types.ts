import { UserProfile, NotificationSetting, UserId } from 'client';


export interface UserProfileDescriptor {
  id: UserId,
  email: string | undefined,
  displayName: string,
  entry: UserProfile,
  created: Date,
  updated: Date,
  notificationSettings: NotificationSetting[];
}



