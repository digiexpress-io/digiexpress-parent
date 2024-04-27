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

export interface BackendAccess {
  accessGranted: boolean;
  message: string;
  required: string[];
}

export type ForbiddenCallback = (access: BackendAccess) => void;



export interface RepoConfig {
  repoId: string;
  repoType: RepoType;
}

export interface Backend {
  store: Store;
  config: StoreConfig;
  health(): Promise<Health>
  withForbidden(handles: (access: BackendAccess) => void): Backend;
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
  withForbidden(handles: ((access: BackendAccess) => void ) | undefined): Store;
  fetch<T>(path: string, init: RequestInit & { notFound?: () => T, repoType: RepoType }): Promise<T>;
}


