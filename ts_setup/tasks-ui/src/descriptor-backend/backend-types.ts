export interface BackendError {
  text: string;
  status: number;
  errors: { id: string; value: string; }[];
}
export type Health = {
  contentType: 'NO_CONNECTION' | 'OK' | 'BACKEND_NOT_FOUND' | 'DIALOB_EXT'
}

export type RepoId = string;
export type RepoType = (
  'PERMISSIONS' |
  'TASKS' |
  'CRM' |
  'STENCIL' |
  'WRENCH' |
  'DIALOB' |
  'CONFIG' |
  'HEALTH' |
  'USER_PROFILE' |
  'TENANT' |
  'SYS_CONFIG' |
  'AVATAR'
  );

export interface RepoConfig {
  repoId: string;
  repoType: RepoType;
}

export interface Backend {
  store: Store;
  config: StoreConfig;
  health(): Promise<Health>
}

export interface StoreConfig {
  urls: Record<RepoType, string>;
  oidc?: string;
  status?: string;
  performInitCheck: boolean
  csrf?: { key: string, value: string }
}
export interface Store {
  config: StoreConfig;
  fetch<T>(path: string, init: RequestInit & { notFound?: () => T, repoType: RepoType }): Promise<T>;
}


