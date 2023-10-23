import { ServiceImpl as ServiceImplAs } from './client-impl';
import { DefaultStore as DefaultStoreAs } from './store-impl';

import {
  BackendError, Backend, StoreConfig, Store,
} from './client-types';

import {
  Org, User, Role, UserId, RoleId
} from './org-types';

import {
  TaskId, Task, TaskPriority, TaskStatus,

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
  TaskCommand, TaskTransaction,

} from './task-types';

import {
  Profile
} from './profile-types';

import {
  ServiceErrorMsg,
  ServiceErrorProps,
  StoreError,
  StoreErrorImpl as StoreErrorImplAs
} from './error-types';


export type {
  Org, User, UserId, Role, RoleId, Profile, Backend, BackendError, StoreError, Task,
  TaskCommand, TaskTransaction,
}

declare namespace TaskClient {
  export type {
    Profile,
    TaskId, Task, TaskPriority, TaskStatus,
    BackendError, Backend, StoreConfig, Store,
    Org, User, Role
  }

  export type {
    ServiceErrorMsg,
    ServiceErrorProps,
    StoreError
  }

  export type {
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
    ChangeChecklistItemTitle
  }
}


namespace TaskClient {
  export const ServiceImpl = ServiceImplAs;
  export const DefaultStore = DefaultStoreAs;
  export const StoreErrorImpl = StoreErrorImplAs;
}
export default TaskClient;