import {
  TaskCommand, TaskCommandType, CreateTask, AssignTaskReporter, ArchiveTask, ChangeTaskStatus, ChangeTaskPriority,
  AssignTaskParent, CommentOnTask, ChangeTaskComment, AssignTaskRoles, AssignTask, ChangeTaskDueDate, ChangeTaskInfo,
  CreateTaskExtension, ChangeTaskExtension, ChangeTaskStartDate, CreateChecklist, ChangeChecklistTitle, DeleteChecklist, AddChecklistItem, DeleteChecklistItem,
  ChangeChecklistItemAssignees, ChangeChecklistItemCompleted, ChangeChecklistItemDueDate, ChangeChecklistItemTitle,
  Task
} from 'taskclient/task-types';

import { TaskDescriptor } from 'taskdescriptor';

export interface SingleEventBody<T extends TaskCommandType, C extends TaskCommand> {
  fromCommand: C | undefined;
  toCommand: C;
  commandType: T;
}

export interface ChangeTaskStartDateEventBody extends SingleEventBody<"ChangeTaskStartDate", ChangeTaskStartDate> { }
export interface CreateTaskEventBody extends SingleEventBody<"CreateTask", CreateTask> { }
export interface AssignTaskReporterEventBody extends SingleEventBody<"AssignTaskReporter", AssignTaskReporter> { }
export interface ArchiveTaskEventBody extends SingleEventBody<"ArchiveTask", ArchiveTask> { }
export interface ChangeTaskStatusEventBody extends SingleEventBody<"ChangeTaskStatus", ChangeTaskStatus> { }
export interface ChangeTaskPriorityEventBody extends SingleEventBody<"ChangeTaskPriority", ChangeTaskPriority> { }
export interface AssignTaskParentEventBody extends SingleEventBody<"AssignTaskParent", AssignTaskParent> { }
export interface CommentOnTaskEventBody extends SingleEventBody<"CommentOnTask", CommentOnTask> { }
export interface ChangeTaskCommentEventBody extends SingleEventBody<"ChangeTaskComment", ChangeTaskComment> { }
export interface AssignTaskRolesEventBody extends SingleEventBody<"AssignTaskRoles", AssignTaskRoles> { }
export interface AssignTaskEventBody extends SingleEventBody<"AssignTask", AssignTask> { }
export interface ChangeTaskDueDateEventBody extends SingleEventBody<"ChangeTaskDueDate", ChangeTaskDueDate> { }
export interface ChangeTaskInfoEventBody extends SingleEventBody<"ChangeTaskInfo", ChangeTaskInfo> { }
export interface CreateTaskExtensionEventBody extends SingleEventBody<"CreateTaskExtension", CreateTaskExtension> { }
export interface ChangeTaskExtensionEventBody extends SingleEventBody<"ChangeTaskExtension", ChangeTaskExtension> { }
export interface CreateChecklistEventBody extends SingleEventBody<"CreateChecklist", CreateChecklist> { }
export interface ChangeChecklistTitleEventBody extends SingleEventBody<"ChangeChecklistTitle", ChangeChecklistTitle> { }
export interface DeleteChecklistEventBody extends SingleEventBody<"DeleteChecklist", DeleteChecklist> { }
export interface AddChecklistItemEventBody extends SingleEventBody<"AddChecklistItem", AddChecklistItem> { }
export interface DeleteChecklistItemEventBody extends SingleEventBody<"DeleteChecklistItem", DeleteChecklistItem> { }
export interface ChangeChecklistItemAssigneesEventBody extends SingleEventBody<"ChangeChecklistItemAssignees", ChangeChecklistItemAssignees> { }
export interface ChangeChecklistItemCompletedEventBody extends SingleEventBody<"ChangeChecklistItemCompleted", ChangeChecklistItemCompleted> { }
export interface ChangeChecklistItemDueDateEventBody extends SingleEventBody<"ChangeChecklistItemDueDate", ChangeChecklistItemDueDate> { }
export interface ChangeChecklistItemTitleEventBody extends SingleEventBody<"ChangeChecklistItemTitle", ChangeChecklistItemTitle> { }





export interface SingleEvent {
  type: "SINGLE";
  body: CreateTaskEventBody |
  ChangeTaskStartDateEventBody |
  AssignTaskReporterEventBody |
  ArchiveTaskEventBody |
  ChangeTaskStatusEventBody |
  ChangeTaskPriorityEventBody |
  AssignTaskParentEventBody |
  CommentOnTaskEventBody |
  ChangeTaskCommentEventBody |
  AssignTaskRolesEventBody |
  AssignTaskEventBody |
  ChangeTaskDueDateEventBody |
  ChangeTaskInfoEventBody |
  CreateTaskExtensionEventBody |
  ChangeTaskExtensionEventBody |
  CreateChecklistEventBody |
  ChangeChecklistTitleEventBody |
  DeleteChecklistEventBody |
  AddChecklistItemEventBody |
  DeleteChecklistItemEventBody |
  ChangeChecklistItemAssigneesEventBody |
  ChangeChecklistItemCompletedEventBody |
  ChangeChecklistItemDueDateEventBody |
  ChangeChecklistItemTitleEventBody;
}

export interface CollapsedEvent {
  items: SingleEvent[];
  type: "COLLAPSED";
}

export type TaskEditEvent = SingleEvent | CollapsedEvent;

export interface TaskEditContextType {
  setState: TaskEditDispatch;
  loading: boolean;
  state: TaskEditState;
}

export type TaskEditMutator = (prev: TaskEditMutatorBuilder) => TaskEditMutatorBuilder;
export type TaskEditDispatch = (mutator: TaskEditMutator) => void;

export interface TaskEditState {
  task: TaskDescriptor;
  events: TaskEditEvent[];
}


export interface TaskEditMutatorBuilder extends TaskEditState {
  withTask(task: Task): TaskEditMutatorBuilder;
  withTaskDescriptor(task: TaskDescriptor): TaskEditMutatorBuilder;
  withCommands(commandsToBeAdded: TaskCommand | TaskCommand[]): TaskEditMutatorBuilder;
}


