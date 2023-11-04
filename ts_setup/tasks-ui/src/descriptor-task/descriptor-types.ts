import { Profile, Task, TaskPriority, TaskStatus, TaskExtension, TaskTransaction, Checklist, TaskComment } from 'client';

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


  assigneeGroupType: AssigneeGroupType | undefined;
  teamGroupType: TeamGroupType | undefined;

  checklist: Checklist[];
  profile: Profile;
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
  profile: Profile;
  roles: string[];
  owners: string[];
}

export interface DescriptorState {
  groupBy: GroupBy;
  groups: Group[];
  filterBy: FilterBy[];
  searchString: string | undefined;

  withTasks(tasks: Data): DescriptorState;
  withGroupBy(groupBy: GroupBy): DescriptorState;
  withSearchString(searchString: string): DescriptorState;
  withFilterByStatus(status: TaskStatus[]): DescriptorState;
  withFilterByPriority(priority: TaskPriority[]): DescriptorState;
  withFilterByOwner(owners: string[]): DescriptorState;
  withFilterByRoles(roles: string[]): DescriptorState;
}
