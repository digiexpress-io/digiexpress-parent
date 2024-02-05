import { UserProfileAndOrg } from './profile-types';
import { ProjectStore } from './project-types';
import { TenantStore } from './tenant-types';
import { Org, User } from './org-types';
import { UserProfileStore } from './profile-types';
import { TenantConfig, RepoType } from './tenant-config-types';


export interface BackendError {
  text: string;
  status: number;
  errors: { id: string; value: string; }[];
}
export type Health = {
  contentType: 'NO_CONNECTION' | 'OK' | 'BACKEND_NOT_FOUND' | 'DIALOB_EXT'
}

export interface Backend {
  store: Store;
  config: StoreConfig;
  project: ProjectStore;
  tenant: TenantStore;
  userProfile: UserProfileStore;

  currentUserProfile(): Promise<UserProfileAndOrg>;
  currentTenant(): Promise<TenantConfig>;
  health(): Promise<Health>
  org(): Promise<{ org: Org, user: User }>;
}

export interface StoreConfig {
  urls: { id: RepoType, url: string }[];
  oidc?: string;
  status?: string;
  performInitCheck: boolean
  csrf?: { key: string, value: string }
}
export interface Store {
  config: StoreConfig;
  withTenantConfig(tenant: TenantConfig): Store;
  fetch<T>(path: string, init: RequestInit & { notFound?: () => T, repoType: RepoType }): Promise<T>;
}


