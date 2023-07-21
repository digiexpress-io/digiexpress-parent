import { ProfileStore } from './profile-types';
import { TaskStore } from './task-types';

export interface ProgramMessage {
  id: string, msg: string
}

export interface Org {
  owners: string[];
  roles: string[];
}

export interface User {
  userId: string;
  userRoles: string[];
  displayName: string;
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


