import { ServiceImpl as ServiceImplAs } from './client-impl';
import { DefaultStore as DefaultStoreAs } from './store-impl';

import {
  BackendError, Backend, StoreConfig, Store,
} from './client-types';

import {
  Org, User, Role, UserId, RoleId,
  resolveAvatar as resolveAvatarAs
} from './org-types';

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
  Checklist, TaskComment

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



declare namespace TaskClient {
  export type {
    Profile,
    TaskId, Task,
    BackendError, Backend, StoreConfig, Store,
    Org, User, Role,
    UserId, RoleId,
    TaskCommand, TaskTransaction,
    TaskExtension,
    Checklist, TaskComment,
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
    ChangeChecklistItemTitle
  }
}

export type {
  Profile,
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
  ChangeChecklistItemTitle
}

const resolveAvatar = resolveAvatarAs;
export {
  resolveAvatar
}

namespace TaskClient {
  export const resolveAvatar = resolveAvatarAs;
  export const ServiceImpl = ServiceImplAs;
  export const DefaultStore = DefaultStoreAs;
  export const StoreErrorImpl = StoreErrorImplAs;
}
export default TaskClient;