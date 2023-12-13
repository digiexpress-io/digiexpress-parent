import {
  UserProfileAndOrg, TaskPriority, TaskStatus, TaskExtension, TaskTransaction, Checklist, TaskComment, TaskCommand, TaskCommandType, CreateTask, AssignTaskReporter,
  ArchiveTask, ChangeTaskStatus, ChangeTaskPriority, AssignTaskParent, CommentOnTask, ChangeTaskComment, AssignTaskRoles, AssignTask, ChangeTaskDueDate,
  ChangeTaskInfo, CreateTaskExtension, ChangeTaskExtension, ChangeTaskStartDate, CreateChecklist, ChangeChecklistTitle, DeleteChecklist,
  AddChecklistItem, DeleteChecklistItem, ChangeChecklistItemAssignees, ChangeChecklistItemCompleted, ChangeChecklistItemDueDate, ChangeChecklistItemTitle,
  Task, CustomerId, UserId, TaskId, RoleId
} from 'client';

export interface SingleEventBody<T extends TaskCommandType, C extends TaskCommand, D> {
  fromCommand: C | undefined;
  toCommand: C;
  commandType: T;
  diff: SingleEventDiff<D>[];
}

export interface SingleEventDiff<T> {
  operation: 'ADDED' | 'REMOVED';
  type: string | undefined
  value: T;
}

export interface ChangeTaskStartDateEventBody extends SingleEventBody<"ChangeTaskStartDate", ChangeTaskStartDate, Date> { }
export interface CreateTaskEventBody extends SingleEventBody<"CreateTask", CreateTask, string> { }
export interface AssignTaskReporterEventBody extends SingleEventBody<"AssignTaskReporter", AssignTaskReporter, UserId> { }
export interface ArchiveTaskEventBody extends SingleEventBody<"ArchiveTask", ArchiveTask, Date> { }
export interface ChangeTaskStatusEventBody extends SingleEventBody<"ChangeTaskStatus", ChangeTaskStatus, TaskStatus> { }
export interface ChangeTaskPriorityEventBody extends SingleEventBody<"ChangeTaskPriority", ChangeTaskPriority, TaskPriority> { }
export interface AssignTaskParentEventBody extends SingleEventBody<"AssignTaskParent", AssignTaskParent, TaskId> { }
export interface CommentOnTaskEventBody extends SingleEventBody<"CommentOnTask", CommentOnTask, string> { }
export interface ChangeTaskCommentEventBody extends SingleEventBody<"ChangeTaskComment", ChangeTaskComment, string> { }
export interface AssignTaskRolesEventBody extends SingleEventBody<"AssignTaskRoles", AssignTaskRoles, RoleId> { }
export interface AssignTaskEventBody extends SingleEventBody<"AssignTask", AssignTask, UserId> { }
export interface ChangeTaskDueDateEventBody extends SingleEventBody<"ChangeTaskDueDate", ChangeTaskDueDate, Date> { }
export interface ChangeTaskInfoEventBody extends SingleEventBody<"ChangeTaskInfo", ChangeTaskInfo, string> { }
export interface CreateTaskExtensionEventBody extends SingleEventBody<"CreateTaskExtension", CreateTaskExtension, string> { }
export interface ChangeTaskExtensionEventBody extends SingleEventBody<"ChangeTaskExtension", ChangeTaskExtension, string> { }
export interface CreateChecklistEventBody extends SingleEventBody<"CreateChecklist", CreateChecklist, string> { }
export interface ChangeChecklistTitleEventBody extends SingleEventBody<"ChangeChecklistTitle", ChangeChecklistTitle, string> { }
export interface DeleteChecklistEventBody extends SingleEventBody<"DeleteChecklist", DeleteChecklist, string> { }
export interface AddChecklistItemEventBody extends SingleEventBody<"AddChecklistItem", AddChecklistItem, string> { }
export interface DeleteChecklistItemEventBody extends SingleEventBody<"DeleteChecklistItem", DeleteChecklistItem, string> { }
export interface ChangeChecklistItemAssigneesEventBody extends SingleEventBody<"ChangeChecklistItemAssignees", ChangeChecklistItemAssignees, UserId> { }
export interface ChangeChecklistItemCompletedEventBody extends SingleEventBody<"ChangeChecklistItemCompleted", ChangeChecklistItemCompleted, boolean> { }
export interface ChangeChecklistItemDueDateEventBody extends SingleEventBody<"ChangeChecklistItemDueDate", ChangeChecklistItemDueDate, Date> { }
export interface ChangeChecklistItemTitleEventBody extends SingleEventBody<"ChangeChecklistItemTitle", ChangeChecklistItemTitle, string> { }





export interface SingleEvent {
  type: "SINGLE";
  targetDate: Date;
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
  targetDate: Date;
  items: SingleEvent[];
  type: "COLLAPSED";
}


export interface AvatarCode {
  twoletters: string;
  value: string;
}

export interface TaskDescriptor {
  entry: Task;
  created: Date;
  id: string;
  status: TaskStatus;
  priority: TaskPriority;
  dueDate: Date | undefined;
  startDate: Date | undefined;
  daysUntilDue: number | undefined; // number in days in which a task is due
  roles: string[];
  assignees: string[];
  labels: string[];
  title: string;
  dialobId: string | undefined;
  description: string;
  comments: TaskComment[];
  transactions: TaskTransaction[];
  uploads: TaskExtension[];
  rolesAvatars: AvatarCode[];
  assigneesAvatars: AvatarCode[];

  customerId: CustomerId | undefined;

  assigneeGroupType: AssigneeGroupType | undefined;
  teamGroupType: TeamGroupType | undefined;

  checklist: Checklist[];
  profile: UserProfileAndOrg;
}

export interface TasksPaletteType {
  roles: Record<string, string>;
  owners: Record<string, string>;
  status: Record<string, string>;
  priority: Record<string, string>;
}


export interface PaletteType {
  assigneeGroupType: Record<AssigneeGroupType, string>,
  teamGroupType: Record<TeamGroupType, string>,
  priority: {
    'HIGH': string,
    'LOW': string,
    'MEDIUM': string
  },
  status: {
    'REJECTED': string,
    'IN_PROGRESS': string,
    'COMPLETED': string,
    'CREATED': string,
  },
  colors: { red: string, green: string, yellow: string, blue: string, violet: string }
}

export type RoleUnassigned = "_nobody_";
export type OwnerUnassigned = "_nobody_";

export type AssigneeGroupType = 'assigneeOverdue' | 'assigneeStartsToday' | 'assigneeOther' | 'assigneeCurrentlyWorking';
export type TeamGroupType = 'groupOverdue' | 'groupAvailable' | 'groupDueSoon';

export type GroupBy = 'status' | 'owners' | 'roles' | 'priority' | 'none' | 'assignee' | 'team';

export type FilterByStatus = { type: 'FilterByStatus', status: TaskStatus[], disabled: boolean }
export type FilterByPriority = { type: 'FilterByPriority', priority: TaskPriority[], disabled: boolean }
export type FilterByOwners = { type: 'FilterByOwners', owners: string[], disabled: boolean }
export type FilterByRoles = { type: 'FilterByRoles', roles: string[], disabled: boolean }
export type FilterBy = FilterByStatus | FilterByPriority | FilterByOwners | FilterByRoles;

export interface Group {
  id: string;
  type: GroupBy;
  color?: string;
  records: TaskDescriptor[];
}

export interface Data {
  tasks: TaskDescriptor[];
  tasksByOwner: Record<string, TaskDescriptor[]>;
  palette: TasksPaletteType;
  profile: UserProfileAndOrg;
  roles: string[];
  owners: string[];
}

export interface TaskGroupsAndFilters {
  groupBy: GroupBy;
  groups: Group[];
  filterBy: FilterBy[];
  searchString: string | undefined;

  withTasks(tasks: Data): TaskGroupsAndFilters;
  withGroupBy(groupBy: GroupBy): TaskGroupsAndFilters;
  withSearchString(searchString: string): TaskGroupsAndFilters;
  withFilterByStatus(status: TaskStatus[]): TaskGroupsAndFilters;
  withFilterByPriority(priority: TaskPriority[]): TaskGroupsAndFilters;
  withFilterByOwner(owners: string[]): TaskGroupsAndFilters;
  withFilterByRoles(roles: string[]): TaskGroupsAndFilters;
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

export interface TasksContextType {
  setState: TasksDispatch;
  reload: () => Promise<void>;
  loading: boolean;
  state: TasksState,
  palette: PaletteType;
}

export type TasksMutator = (prev: TasksState) => TasksState;
export type TasksDispatch = (mutator: TasksMutator) => void;

export interface TasksState {
  tasks: TaskDescriptor[];
  tasksByOwner: Record<string, TaskDescriptor[]>;
  palette: TasksPaletteType;
  profile: UserProfileAndOrg;
  roles: string[];
  owners: string[];

  withProfile(profile: UserProfileAndOrg): TasksState;
  withTasks(tasks: Task[]): TasksState;
  toGroupsAndFilters(): TaskGroupsAndFilters;
}

