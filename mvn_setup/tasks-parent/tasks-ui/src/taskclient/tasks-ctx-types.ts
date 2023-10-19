import { Task, TaskPriority, TaskStatus, TaskExtension, TaskTransaction, Checklist, TaskComment } from './task-types';
import { Profile } from './profile-types';

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
  dialobId: string;
  description: string;
  comments: TaskComment[];
  transactions: TaskTransaction[];
  uploads: TaskExtension[];
  rolesAvatars: AvatarCode[];
  assigneesAvatars: AvatarCode[];


  assigneeGroupType: AssigneeGroupType | undefined;
  teamGroupType: TeamGroupType | undefined;

  checklist: Checklist[];
  profile: Profile;
}

export interface PalleteType {
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
  assigneeGroupType: Record<AssigneeGroupType, string>,
  teamGroupType: Record<TeamGroupType, string>,
  colors: { red: string, green: string, yellow: string, blue: string, violet: string }
}

export interface TasksContextType {
  setState: TasksDispatch;
  reload: () => Promise<void>;
  loading: boolean;
  state: TasksState,
  pallette: PalleteType;
}

export type RoleUnassigned = "_nobody_";
export type OwnerUnassigned = "_nobody_";

export type TasksMutator = (prev: TasksMutatorBuilder) => TasksMutatorBuilder;
export type TasksDispatch = (mutator: TasksMutator) => void;

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

export interface TasksState {
  tasks: TaskDescriptor[];
  tasksByOwner: Record<string, TaskDescriptor[]>;
  groupBy: GroupBy;
  groups: Group[];
  filterBy: FilterBy[];
  searchString: string | undefined;
  pallette: TasksStatePallette;
  withGroupBy(groupBy: GroupBy): TasksMutatorBuilder;
}

export interface TasksStatePallette {
  roles: Record<string, string>;
  owners: Record<string, string>;
  status: Record<string, string>;
  priority: Record<string, string>;
}

export interface TasksMutatorBuilder extends TasksState {
  withProfile(profile: Profile): TasksMutatorBuilder;
  withTasks(tasks: Task[]): TasksMutatorBuilder;
  withSearchString(searchString: string): TasksMutatorBuilder;
  withFilterByStatus(status: TaskStatus[]): TasksMutatorBuilder;
  withFilterByPriority(priority: TaskPriority[]): TasksMutatorBuilder;
  withFilterByOwner(owners: string[]): TasksMutatorBuilder;
  withFilterByRoles(roles: string[]): TasksMutatorBuilder;
}


