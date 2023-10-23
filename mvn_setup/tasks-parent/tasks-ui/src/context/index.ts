import { ClientContextType, ComposerContextType } from './client-ctx';
import * as Hooks from './hooks';
import * as taskEditCtx from './task-edit-ctx';
import * as descCtx from 'taskdescriptor';

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

import {
  TasksContextType, TasksState
} from './tasks-ctx-types';


import {
  Document
} from './composer-ctx-types';


export type { Document }
export { useComposer } from './hooks';

export {
  initSession, SessionData, ActionsImpl
} from './composer-ctx-impl';



declare namespace Context {
  export type {
    ClientContextType, ComposerContextType,
    TasksContextType, TasksState,
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



namespace Context {
  export const EditProvider = taskEditCtx.TaskEditProvider;
  export const DescriptorStateImpl = descCtx.DescriptorStateImpl;
  export const StatusPalette = descCtx.Palette.status;
  export const PriorityPalette = descCtx.Palette.priority;
  export const TeamGroupPalette = descCtx.Palette.teamGroupType;
  export const AssigneePalette = descCtx.Palette.assigneeGroupType;
  export const _nobody_ = descCtx._nobody_;

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

}

export default Context;