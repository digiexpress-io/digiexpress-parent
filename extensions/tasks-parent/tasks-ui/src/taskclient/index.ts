import { ServiceImpl as ServiceImplAs } from './client';
import { DefaultStore as DefaultStoreAs } from './client-store';

import { TablePagination, TablePagination as TablePaginationAs } from './table-pagination';


import {
  DescriptorTableStateBuilder, DescriptorTableContextType, DescriptorTableState,
  Provider as TableProviderAs,
  useTable as useTableAs,
  CustomTable
} from './table-ctx';

//export { Provider, useTable };
//export type { DescriptorTableStateBuilder, DescriptorTableContextType, DescriptorTableState };

import {
  ClientError, Client, StoreConfig, Store,
  ProgramMessage, Org, User
} from './client-types';

import {
  TaskId, Task, TaskPriority, TaskStatus,
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
  TasksStatePallette,
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
  SingleEvent, CollapsedEvent


} from './task-edit-ctx-types';


import * as taskCtxImpl from './tasks-ctx-impl';
import * as taskEditCtx from './task-edit-ctx';


import * as Hooks from './hooks';


declare namespace TaskClient {
  export type { DescriptorTableStateBuilder, DescriptorTableContextType, DescriptorTableState };
  export type { TablePagination };
  export type { ClientContextType, ComposerContextType };
  export type {
    Profile,
    TaskId, Task, TaskPriority, TaskStatus,
    ClientError, Client, StoreConfig, Store,
    Org, User,
    ProgramMessage
  }

  export type {
    DocumentId, Document, DocumentUpdate,
    TabEntity, TabBody, Tab,
    PageUpdate, Session, Actions
  }

  export type {
    ServiceErrorMsg,
    ServiceErrorProps,
    StoreError
  }

  export type {
    TaskDescriptor, TasksContextType, TasksState, TasksMutatorBuilder,
    PalleteType, FilterBy, Group, GroupBy, RoleUnassigned, OwnerUnassigned,
    TasksStatePallette
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
}


namespace TaskClient {
  export const TableProvider = TableProviderAs;
  export const TablePaginationImpl = TablePaginationAs;
  export const ServiceImpl = ServiceImplAs;
  export const DefaultStore = DefaultStoreAs;
  export const StoreErrorImpl = StoreErrorImplAs;
  export const Error = ErrorView;
  export const Provider = ProviderImpl;
  export const EditProvider = taskEditCtx.TaskEditProvider;
  export const useTable = useTableAs;
  export const useBackend = Hooks.useBackend;
  export const useTasks = Hooks.useTasks;
  export const useOrg = Hooks.useOrg;
  export const useTaskEdit = Hooks.useTaskEdit;
  export const useSite = Hooks.useSite;
  export const useUnsaved = Hooks.useUnsaved;
  export const useComposer = Hooks.useComposer;
  export const useSession = Hooks.useSession;
  export const useNav = Hooks.useNav;
  export const _nobody_ = taskCtxImpl._nobody_;
  export const Table = CustomTable;
}

export default TaskClient;