import { ProfileStore } from './profile-types';
import { TaskStore } from './task-types';
import { ProjectStore } from './project-types';
import { TenantStore } from './tenant-types';
import { Org, User } from './org-types';

export interface BackendError {
  text: string;
  status: number;
  errors: { id: string; value: string; }[];
}


export interface Backend {
  config: StoreConfig;
  profile: ProfileStore;
  task: TaskStore;
  project: ProjectStore;
  tenant: TenantStore;
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
  withProjectId(projectId: string): Store;
  fetch<T>(path: string, init?: RequestInit & { notFound?: () => T }): Promise<T>;
}


