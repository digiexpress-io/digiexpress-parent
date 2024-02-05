import { ServiceImpl as ServiceImplAs } from './backend-impl';
import { DefaultStore as DefaultStoreAs } from './backend-store-impl';

import {
  BackendError, Backend, StoreConfig, Store,
} from './backend-types';

import {
  Org, User, Role, UserId, RoleId
} from './org-types';


import {
  FormTechnicalName, FormTitle,
  Tenant, TenantEntry,
  TenantId, TenantStore,
  DialobTag, DialobForm, DialobVariable, DialobSession
} from './tenant-types';

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
    DialobTag, DialobForm,
    BackendError, Backend, StoreConfig, Store,
    Org, User, Role,
    UserId, RoleId,

    ServiceErrorMsg,
    ServiceErrorProps,
    StoreError,
    FormTechnicalName, FormTitle,
    Tenant, TenantEntry,
    TenantId, TenantStore,
    DialobSession,

    RepoType, TenantConfigId, TenantConfig, AppType
  }
}

export type {
  RepoType,
  BackendError, Backend, StoreConfig, Store, StoreError,
  Org, User, Role,
  UserId, RoleId,

  FormTechnicalName, FormTitle,
  Tenant, TenantEntry,
  TenantId, TenantStore,
  DialobTag, DialobForm, DialobVariable, DialobSession,

  TenantConfigId, TenantConfig, AppType
}

namespace TaskClient {
  export const ServiceImpl = ServiceImplAs;
  export const DefaultStore = DefaultStoreAs;
  export const StoreErrorImpl = StoreErrorImplAs;
}
export default TaskClient;