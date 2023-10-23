export type UserEventType = 'task-completed' | 'message-sent' | 'checklist-completed' | 'checklist-self-assigned' | 'comment-finished' | 'task-blocked' | 'attachment-added';

export interface UserActivity{
  id: string;
  eventDate: string;
  eventType: UserEventType;
  subjectTitle: string;
}

export type UserId = string;

export type RoleId = string;

export interface Org {
  users: Record<UserId, User>;
  roles: Record<RoleId, Role>;
}

export interface Role {
  roleId: RoleId;
  avatar: string;
  displayName: string;
}

export interface User {
  userId: UserId;
  userRoles: RoleId[];
  displayName: string;
  avatar: string;
  activity: UserActivity[];
}
