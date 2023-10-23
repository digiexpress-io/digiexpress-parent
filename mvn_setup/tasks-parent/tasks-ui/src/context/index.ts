import { ClientContextType, ComposerContextType } from './client-ctx';

import * as taskCtxImpl from './tasks-ctx-impl';

import * as Hooks from './hooks';

import * as screenCtx from './screen-ctx';

import * as taskEditCtx from './task-edit-ctx';

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

import { ScreenState } from './screen-ctx-types';

import {
  TaskDescriptor, TasksContextType, TasksState, TasksMutatorBuilder,
  PalleteType, FilterBy, Group, GroupBy, RoleUnassigned, OwnerUnassigned,
  TasksStatePallette, TeamGroupType, AssigneeGroupType, AvatarCode,
  FilterByRoles, FilterByOwners, FilterByStatus, FilterByPriority
} from './tasks-ctx-types';


import {
  Document
} from './composer-ctx-types';


export type {
  Document
}
export { useComposer } from './hooks';

export {
  initSession, SessionData, ActionsImpl
} from './composer-ctx-impl';



declare namespace Context {
  export type { ClientContextType, ComposerContextType };
  export type {
    AvatarCode,
    FilterByRoles, FilterByOwners, FilterByStatus, FilterByPriority
  };
  export type {
    TaskDescriptor, TasksContextType, TasksState, TasksMutatorBuilder,
    PalleteType, FilterBy, Group, GroupBy, RoleUnassigned, OwnerUnassigned,
    TasksStatePallette, TeamGroupType, AssigneeGroupType
  };
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
    ScreenState
  };
}



namespace Context {
  export const StatusPallette = taskCtxImpl.Pallette.status;
  export const PriorityPalette = taskCtxImpl.Pallette.priority;
  export const TeamGroupPallete = taskCtxImpl.Pallette.teamGroupType;
  export const AssigneePalette = taskCtxImpl.Pallette.assigneeGroupType;
  export const EditProvider = taskEditCtx.TaskEditProvider;

  export const _nobody_ = taskCtxImpl._nobody_;
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
  export const ScreenProvider = screenCtx.ScreenProvider;
  export const useScreen = Hooks.useScreen;

}

export default Context;