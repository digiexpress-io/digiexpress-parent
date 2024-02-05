import { ServiceImpl as ServiceImplAs } from './backend-impl';
import { DefaultStore as DefaultStoreAs } from './backend-store-impl';

import {
  BackendError, Backend, StoreConfig, Store,
} from './backend-types';

import {
  Org, User, Role, UserId, RoleId
} from './org-types';


import {
  ServiceErrorMsg,
  ServiceErrorProps,
  StoreError,
  StoreErrorImpl as StoreErrorImplAs
} from './error-types';

import {
  RepoType, TenantConfigId, TenantConfig, AppType
} from './tenant-config-types';

declare namespace TaskClient {
  export type {
    BackendError, Backend, StoreConfig, Store,
    Org, User, Role,
    UserId, RoleId,

    ServiceErrorMsg,
    ServiceErrorProps,
    StoreError,
    RepoType, TenantConfigId, TenantConfig, AppType
  }
}

export type {
  RepoType,
  BackendError, Backend, StoreConfig, Store, StoreError,
  Org, User, Role,
  UserId, RoleId,
  TenantConfigId, TenantConfig, AppType
}

namespace TaskClient {
  export const ServiceImpl = ServiceImplAs;
  export const DefaultStore = DefaultStoreAs;
  export const StoreErrorImpl = StoreErrorImplAs;
}
export default TaskClient;