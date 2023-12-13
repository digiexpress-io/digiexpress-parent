import { UserProfile, NotificationSetting, UserId } from 'client';


export interface UserProfileDescriptor {
  id: UserId,
  entry: UserProfile,
  created: Date,
  notificationSettings: NotificationSetting[];
}



