import { ServiceImpl as ServiceImplAs } from './backend-impl';
import { DefaultStore as DefaultStoreAs } from './backend-store-impl';

import {
  BackendError, Backend, StoreConfig, Store,
} from './backend-types';

import {
  Org, User, Role, UserId, RoleId
} from './org-types';

import {
  UserProfileAndOrg
} from './profile-types';

import {
  Project, ProjectId,
  ChangeProjectInfo,
  AssignProjectUsers,
  CreateProject,
  ArchiveProject,
  ChangeRepoType
} from './project-types';

import {
  FormTechnicalName, FormTitle,
  Tenant, TenantEntry,
  TenantId, TenantStore,
  DialobTag, DialobForm, DialobVariable, DialobSession
} from './tenant-types';


import {
  Customer, CustomerId, CustomerTask, CustomerContact,
  CustomerCommandType, CustomerCommand, CustomerBodyType,
  CreateCustomer,
  UpsertSuomiFiPerson,
  UpsertSuomiFiRep,
  ChangeCustomerFirstName,
  ChangeCustomerLastName,
  ChangeCustomerSsn,
  ChangeCustomerEmail,
  ChangeCustomerAddress,
  ArchiveCustomer, Person, Company
} from './customer-types';

import {
  UserProfile, NotificationSetting, ChangeUserDetailsFirstName, UpsertUiSettings, UiSettings
} from './profile-types';

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
    UserProfile,
    Project, ProjectId,
    ChangeProjectInfo,
    AssignProjectUsers,
    CreateProject,
    ArchiveProject,
    ChangeRepoType,

    Person, Company,
    Customer, CustomerId, CustomerTask, CustomerBodyType,
    CustomerCommandType, CustomerCommand, CustomerContact,
    CreateCustomer,
    UpsertSuomiFiPerson,
    UpsertSuomiFiRep,
    ChangeCustomerFirstName,
    ChangeCustomerLastName,
    ChangeCustomerSsn,
    ChangeCustomerEmail,
    ChangeCustomerAddress,
    ArchiveCustomer,

    UserProfileAndOrg, NotificationSetting, ChangeUserDetailsFirstName,


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
  UpsertUiSettings, UiSettings,
  Customer, CustomerId, CustomerTask, CustomerBodyType,
  CustomerCommandType, CustomerCommand, CustomerContact,
  CreateCustomer,
  UpsertSuomiFiPerson,
  UpsertSuomiFiRep,
  ChangeCustomerFirstName,
  ChangeCustomerLastName,
  ChangeCustomerSsn,
  ChangeCustomerEmail,
  ChangeCustomerAddress,
  ArchiveCustomer,

  UserProfileAndOrg, NotificationSetting, ChangeUserDetailsFirstName,

  Person, Company,
  Project, ProjectId, RepoType,
  UserProfile,
  BackendError, Backend, StoreConfig, Store, StoreError,
  Org, User, Role,
  UserId, RoleId,

  ChangeProjectInfo,
  AssignProjectUsers,
  CreateProject,
  ArchiveProject,
  ChangeRepoType,

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