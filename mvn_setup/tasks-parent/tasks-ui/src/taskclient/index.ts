import { ServiceImpl as ServiceImplAs } from './client';
import { DefaultStore as DefaultStoreAs } from './client-store';

import { TablePagination, TablePagination as TablePaginationAs } from './table-pagination';

import {
  ClientError, Client, StoreConfig, Store,
  ProgramMessage, Org, User, Role
} from './client-types';

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
  ChangeChecklistItemTitle

} from './task-types';

import {
  Profile
} from './profile-types';


import {
  DocumentId, Document, DocumentUpdate,
  TabEntity, TabBody, Tab,
  PageUpdate, Session, Actions,
} from './composer-types';

import {
  ServiceErrorMsg,
  ServiceErrorProps,
  StoreError,
  StoreErrorImpl as StoreErrorImplAs
} from './error-types';


import ErrorView from './Components/ErrorView';
import { ClientContextType, ComposerContextType } from './client-ctx';
import ProviderImpl from './Provider';

import {
  TaskDescriptor, TasksContextType, TasksState, TasksMutatorBuilder,
  PalleteType, FilterBy, Group, GroupBy, RoleUnassigned, OwnerUnassigned,
  TasksStatePallette, TeamGroupType, AssigneeGroupType, AvatarCode
} from './tasks-ctx-types';

import {
  TaskEditEvent, TaskEditMutatorBuilder, TaskEditState,
  CreateTaskEventBody,
  AssignTaskReporterEventBody,
  ArchiveTaskEventBody,
  ChangeTaskStatusEventBody,
  ChangeTaskPriorityEventBody,
  AssignTaskParentEventBody,
  CommentOnTaskEventBody,
  ChangeTaskCommentEventBody,
  AssignTaskRolesEventBody,
  AssignTaskEventBody,
  ChangeTaskDueDateEventBody,
  ChangeTaskInfoEventBody,
  CreateTaskExtensionEventBody,
  ChangeTaskExtensionEventBody,
  SingleEvent, CollapsedEvent,

} from './task-edit-ctx-types';


import * as taskCtxImpl from './tasks-ctx-impl';
import * as taskEditCtx from './task-edit-ctx';
import * as screenCtx from './screen-ctx';
import { ScreenState } from './screen-ctx-types';

import * as Hooks from './hooks';

declare namespace TaskClient {
  export type { TablePagination };
  export type { ClientContextType, ComposerContextType };
  export type {
    Profile,
    TaskId, Task, TaskPriority, TaskStatus,
    ClientError, Client, StoreConfig, Store,
    Org, User, Role, AvatarCode,
    ProgramMessage
  }

  export type {
    DocumentId, Document, DocumentUpdate,
    TabEntity, TabBody, Tab,
    PageUpdate, Session, Actions
  }

  export type {
    ScreenState
  }

  export type {
    ServiceErrorMsg,
    ServiceErrorProps,
    StoreError
  }

  export type {
    TaskDescriptor, TasksContextType, TasksState, TasksMutatorBuilder,
    PalleteType, FilterBy, Group, GroupBy, RoleUnassigned, OwnerUnassigned,
    TasksStatePallette, TeamGroupType, AssigneeGroupType
  }
  export type {
    TaskEditEvent, TaskEditMutatorBuilder, TaskEditState,
    CreateTaskEventBody,
    AssignTaskReporterEventBody,
    ArchiveTaskEventBody,
    ChangeTaskStatusEventBody,
    ChangeTaskPriorityEventBody,
    AssignTaskParentEventBody,
    CommentOnTaskEventBody,
    ChangeTaskCommentEventBody,
    AssignTaskRolesEventBody,
    AssignTaskEventBody,
    ChangeTaskDueDateEventBody,
    ChangeTaskInfoEventBody,
    CreateTaskExtensionEventBody,
    ChangeTaskExtensionEventBody,
    SingleEvent, CollapsedEvent
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
  export const TablePaginationImpl = TablePaginationAs;
  export const ServiceImpl = ServiceImplAs;
  export const DefaultStore = DefaultStoreAs;
  export const StoreErrorImpl = StoreErrorImplAs;
  export const StatusPallette = taskCtxImpl.Pallette.status;
  export const PriorityPalette = taskCtxImpl.Pallette.priority;
  export const TeamGroupPallete = taskCtxImpl.Pallette.teamGroupType;
  export const AssigneePalette = taskCtxImpl.Pallette.assigneeGroupType;
  export const Error = ErrorView;
  export const Provider = ProviderImpl;
  export const EditProvider = taskEditCtx.TaskEditProvider;
  export const useBackend = Hooks.useBackend;
  export const useTasks = Hooks.useTasks;
  export const useOrg = Hooks.useOrg;
  export const useAssignees = Hooks.useAssignees;
  export const useRoles = Hooks.useRoles;
  export const useTaskEdit = Hooks.useTaskEdit;
  export const useSite = Hooks.useSite;
  export const useUnsaved = Hooks.useUnsaved;
  export const useComposer = Hooks.useComposer;
  export const useSession = Hooks.useSession;
  export const useNav = Hooks.useNav;
  export const _nobody_ = taskCtxImpl._nobody_;
  export const ScreenProvider = screenCtx.ScreenProvider;
  export const useScreen = Hooks.useScreen;
}

export default TaskClient;