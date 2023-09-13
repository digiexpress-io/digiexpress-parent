import { ProfileStore } from './profile-types';
import { TaskStore } from './task-types';

export type UserEventType = 'task-completed' | 'message-sent' | 'checklist-completed' | 'checklist-self-assigned' | 'comment-finished' | 'task-blocked' | 'attachment-added';

export interface UserActivity{
  id: string;
  eventDate: string;
  eventType: UserEventType;
  subjectTitle: string;
}

export interface ProgramMessage {
  id: string, msg: string
}

export type UserId = string;

export interface Org {
  users: Record<UserId, User>;
  roles: string[];
}

export interface User {
  userId: UserId;
  userRoles: string[];
  displayName: string;
  avatar: string;
  activity: UserActivity[];
}

export interface ClientError {
  text: string;
  status: number;
  errors: { id: string; value: string; }[];
}


export interface Client {
  config: StoreConfig;
  profile: ProfileStore;
  task: TaskStore;

  org(): Promise<{ org: Org, user: User }>;
}

export interface StoreConfig {
  url: string;
  oidc?: string;
  status?: string;
  csrf?: { key: string, value: string }
}
export interface Store {
  config: StoreConfig;
  fetch<T>(path: string, init?: RequestInit & { notFound?: () => T }): Promise<T>;
}


