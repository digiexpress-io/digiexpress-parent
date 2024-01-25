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
  TaskPriority, TaskStatus,
  TaskId, Task,
  CreateTask,
  ChangeTaskStartDate,
  AssignTaskReporter,
  ArchiveTask,
  ChangeTaskStatus,
  ChangeTaskPriority,
  AssignTaskParent,
  CommentOnTask,
  ChangeTaskComment,
  AssignTaskRoles,
  AssignTask,
  ChangeTaskDueDate,
  ChangeTaskInfo,
  CreateTaskExtension,
  ChangeTaskExtension,
  CreateChecklist,
  ChangeChecklistTitle,
  DeleteChecklist,
  AddChecklistItem,
  DeleteChecklistItem,
  ChangeChecklistItemAssignees,
  ChangeChecklistItemCompleted,
  ChangeChecklistItemDueDate,
  ChangeChecklistItemTitle,
  TaskCommand, TaskTransaction, TaskExtension,
  Checklist, TaskComment, TaskUpdateCommand,
  TaskCommandType
} from './task-types';

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
  UserProfile, NotificationSetting, ChangeUserDetailsFirstName
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

    TaskId, Task,
    BackendError, Backend, StoreConfig, Store,
    Org, User, Role,
    UserId, RoleId,
    TaskCommand, TaskTransaction,
    TaskExtension,
    Checklist, TaskComment, TaskCommandType,
    TaskPriority, TaskStatus,
    ServiceErrorMsg,
    ServiceErrorProps,
    StoreError,
    CreateTask,
    ChangeTaskStartDate,
    AssignTaskReporter,
    ArchiveTask,
    ChangeTaskStatus,
    ChangeTaskPriority,
    AssignTaskParent,
    CommentOnTask,
    ChangeTaskComment,
    AssignTaskRoles,
    AssignTask,
    ChangeTaskDueDate,
    ChangeTaskInfo,
    CreateTaskExtension,
    ChangeTaskExtension,
    CreateChecklist,
    ChangeChecklistTitle,
    DeleteChecklist,
    AddChecklistItem,
    DeleteChecklistItem,
    ChangeChecklistItemAssignees,
    ChangeChecklistItemCompleted,
    ChangeChecklistItemDueDate,
    ChangeChecklistItemTitle,
    TaskUpdateCommand,
    FormTechnicalName, FormTitle,
    Tenant, TenantEntry,
    TenantId, TenantStore,
    DialobSession,

    RepoType, TenantConfigId, TenantConfig, AppType
  }
}

export type {
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

  TaskCommandType,
  Person, Company,
  Project, ProjectId, RepoType,
  UserProfile,
  TaskId, Task,
  BackendError, Backend, StoreConfig, Store, StoreError,
  Org, User, Role,
  UserId, RoleId,
  TaskCommand, TaskTransaction,
  TaskExtension,
  Checklist, TaskComment,
  TaskPriority, TaskStatus,
  CreateTask,
  ChangeTaskStartDate,
  AssignTaskReporter,
  ArchiveTask,
  ChangeTaskStatus,
  ChangeTaskPriority,
  AssignTaskParent,
  CommentOnTask,
  ChangeTaskComment,
  AssignTaskRoles,
  AssignTask,
  ChangeTaskDueDate,
  ChangeTaskInfo,
  CreateTaskExtension,
  ChangeTaskExtension,
  CreateChecklist,
  ChangeChecklistTitle,
  DeleteChecklist,
  AddChecklistItem,
  DeleteChecklistItem,
  ChangeChecklistItemAssignees,
  ChangeChecklistItemCompleted,
  ChangeChecklistItemDueDate,
  ChangeChecklistItemTitle,
  TaskUpdateCommand,

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