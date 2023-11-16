import { Task, CustomerId, UserProfile } from 'client';


export interface CustomerCaseDescriptor {
  id: CustomerId;
  firstName: string;
  lastName: string;
  ssn: string;

  task: Task;

  profile: UserProfile;
}

export interface AvatarCode {
  twoletters: string;
  value: string;
}

export interface CustomerPaletteType {
  customers: Record<string, string>;
  assignee: Record<string, string>;
  role: Record<string, string>;
}
export interface PaletteType {
  colors: { red: string, green: string, yellow: string, blue: string, violet: string }
}

export type FilterByAssignees = { type: 'FilterByAssignees', assignees: string[], disabled: boolean }
export type FilterByRoles = { type: 'FilterByRoles', roles: string[], disabled: boolean }
export type FilterBy = FilterByAssignees | FilterByRoles;

export type GroupBy = 'assignees' | 'roles' | 'none';

export interface Group {
  id: string;
  type: GroupBy;
  color?: string;
  records: CustomerCaseDescriptor[];
}

export interface Data {
  cases: CustomerCaseDescriptor[];
  casesByUser: Record<string, CustomerCaseDescriptor[]>;
  palette: CustomerPaletteType;
  assignees: string[];
  roles: string[];
  profile: UserProfile;
}

export interface DescriptorState {
  groupBy: GroupBy;
  groups: Group[];
  filterBy: FilterBy[];
  searchString: string | undefined;

  withSearchString(searchString: string): DescriptorState;
  withCases(data: Data): DescriptorState;
  withGroupBy(groupBy: GroupBy): DescriptorState;
  withFilterByAssignees(assignees: string[]): DescriptorState;
  withFilterByRoles(roles: string[]): DescriptorState;
}