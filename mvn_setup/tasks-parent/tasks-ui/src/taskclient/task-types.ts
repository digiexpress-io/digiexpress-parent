export type TaskId = string;


export interface Task {
  readonly created: string;
  readonly updated: string;
  readonly archived: string | undefined;
  readonly startDate: string | undefined;
  readonly dueDate: string | undefined;

  readonly parentId: string | undefined;
  readonly transactions: TaskTransaction[];
  readonly roles: string[];
  readonly assigneeIds: string[];
  readonly reporterId: string;

  readonly title: string;
  readonly description: string;
  readonly priority: TaskPriority;
  readonly status: TaskStatus;
  readonly labels: string[];
  readonly extensions: TaskExtension[];
  readonly comments: TaskComment[];

  readonly checklist: Checklist[];

  readonly id: TaskId;
  readonly version: string;
  readonly documentType: 'TASK';
}


export type TaskStatus = 'CREATED' | 'IN_PROGRESS' | 'COMPLETED' | 'REJECTED';
export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH';

export interface Checklist {
  id: string;
  title: string;
  items: ChecklistItem[];
}

export interface ChecklistItem {
  id: string;
  title: string;
  completed: boolean;
  assigneeIds: string[];
  dueDate: string | undefined;
}

export interface TaskTransaction {
  id: string;
  commands: TaskCommand[];
}

export interface TaskExtension {
  id: string;
  type: 'dialob' | 'upload' | string;
  name: string;
  body: string;
  created: string;
  updated: string;
}

export interface TaskComment {
  id: string;
  created: Date;
  replyToId: string | undefined;
  commentText: string;
  username: string;
}

export interface TaskHistory {
  id: string;
  versions: Task[];
}

export interface TaskCommand {
  targetDate: string | undefined;
  userId: string;
  commandType: TaskCommandType;
}

export type TaskCommandType =
  'CreateTask' | 'ChangeTaskStatus' | 'ChangeTaskPriority' | 'AssignTaskReporter' | 'ArchiveTask' |
  'CommentOnTask' | 'ChangeTaskComment' | 'AssignTaskRoles' | 'AssignTask' | 'ChangeTaskStartDate' | 'ChangeTaskDueDate' | 
  'AssignTaskParent' | 'ChangeTaskInfo' | 'CreateTaskExtension' | 'ChangeTaskExtension' |
  'CreateChecklist' | 'ChangeChecklistTitle' | 'DeleteChecklist' | 'AddChecklistItem' | 'DeleteChecklistItem' | 
  'ChangeChecklistItemAssignees' | 'ChangeChecklistItemCompleted' | 'ChangeChecklistItemDueDate' | 'ChangeChecklistItemTitle';

export interface TaskUpdateCommand<T extends TaskCommandType> extends TaskCommand {
  taskId: TaskId;
  commandType: T;
}

export interface CreateTask extends TaskCommand {
  commandType: 'CreateTask';
  roles: string[];
  assigneeIds: string[];
  reporterId: string;
  status: TaskStatus | undefined;
  startDate: string | undefined;
  dueDate: string | undefined;
  title: string;
  description: string;
  priority: TaskPriority;
  labels: string[];
  extensions: TaskExtension[];
  comments: TaskComment[];
  checklist: Checklist[];
}

export interface AssignTaskReporter extends TaskUpdateCommand<'AssignTaskReporter'> {
  reporterId: string;
}

export interface ArchiveTask extends TaskUpdateCommand<'ArchiveTask'> {
}

export interface ChangeTaskStatus extends TaskUpdateCommand<'ChangeTaskStatus'> {
  status: TaskStatus;
}

export interface ChangeTaskPriority extends TaskUpdateCommand<'ChangeTaskPriority'> {
  priority: TaskPriority;
}

export interface AssignTaskParent extends TaskUpdateCommand<'AssignTaskParent'> {
  parentId: string;
}

export interface CommentOnTask extends TaskUpdateCommand<'CommentOnTask'> {
  replyToCommentId: string | undefined;
  commentText: string;
}

export interface ChangeTaskComment extends TaskUpdateCommand<'ChangeTaskComment'> {
  commentId: string;
  replyToCommentId: string | undefined;
  commentText: string;
}

export interface AssignTaskRoles extends TaskUpdateCommand<'AssignTaskRoles'> {
  roles: string[];
}

export interface AssignTask extends TaskUpdateCommand<'AssignTask'> {
  assigneeIds: string[];
}

export interface ChangeTaskStartDate extends TaskUpdateCommand<'ChangeTaskStartDate'> {
  startDate: string;
}

export interface ChangeTaskDueDate extends TaskUpdateCommand<'ChangeTaskDueDate'> {
  dueDate: string;
}

export interface ChangeTaskInfo extends TaskUpdateCommand<'ChangeTaskInfo'> {
  title: string;
  description: string
}

export interface CreateTaskExtension extends TaskUpdateCommand<'CreateTaskExtension'> {
  type: string;
  name: string;
  body: string;
}

export interface ChangeTaskExtension extends TaskUpdateCommand<'ChangeTaskExtension'> {
  id: TaskId;
  type: string;
  name: string;
  body: string;
}

export interface CreateChecklist extends TaskUpdateCommand<'CreateChecklist'> {
  title: string;
  checklist: ChecklistItem[];
}

export interface ChangeChecklistTitle extends TaskUpdateCommand<'ChangeChecklistTitle'> {
  checklistId: string;
  title: string;
}

export interface DeleteChecklist extends TaskUpdateCommand<'DeleteChecklist'> {
  checklistId: string;
}

export interface AddChecklistItem extends TaskUpdateCommand<'AddChecklistItem'> {
  checklistId: string;
  title: string;
  assigneeIds: string[];
  dueDate: string | undefined;
  completed: boolean;
}

export interface DeleteChecklistItem extends TaskUpdateCommand<'DeleteChecklistItem'> {
  checklistId: string;
  checklistItemId: string;
}

export interface ChangeChecklistItemAssignees extends TaskUpdateCommand<'ChangeChecklistItemAssignees'> {
  checklistId: string;
  checklistItemId: string;
  assigneeIds: string[];
}

export interface ChangeChecklistItemCompleted extends TaskUpdateCommand<'ChangeChecklistItemCompleted'> {
  checklistId: string;
  checklistItemId: string;
  completed: boolean;
}

export interface ChangeChecklistItemDueDate extends TaskUpdateCommand<'ChangeChecklistItemDueDate'> {
  checklistId: string;
  checklistItemId: string;
  dueDate: string | undefined;
}

export interface ChangeChecklistItemTitle extends TaskUpdateCommand<'ChangeChecklistItemTitle'> {
  checklistId: string;
  checklistItemId: string;
  title: string;
}

export interface TaskPagination {
  page: number; //starts from 1
  total: { pages: number, records: number };
  records: Task[];
}

export interface TaskStore {
  getActiveTasks(): Promise<TaskPagination>
  getActiveTask(id: TaskId): Promise<Task>
}

