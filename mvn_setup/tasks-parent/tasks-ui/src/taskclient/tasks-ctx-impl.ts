import { parseISO, isAfter, isEqual, differenceInCalendarDays } from 'date-fns';

import { Task, TaskExtension, TaskPriority, TaskStatus } from './task-types';
import {
  PalleteType, TasksState, TasksMutatorBuilder, TaskDescriptor, FilterBy, Group, GroupBy,
  RoleUnassigned, OwnerUnassigned, TasksStatePallette,
  FilterByOwners, FilterByPriority, FilterByRoles, FilterByStatus, AvatarCode, AssigneeGroupType, TeamGroupType
} from './tasks-ctx-types';

import { Profile } from './profile-types';

const _nobody_: RoleUnassigned & OwnerUnassigned = '_nobody_';

// https://coolors.co/ff595e-26c485-ffca3a-1982c4-6a4c93
const bittersweet: string = '#FF595E'; //red
const emerald: string = '#26C485';     //green
const sunglow: string = '#FFCA3A';     //yellow
const steelblue: string = '#1982C4';   //blue
const ultraviolet: string = '#6A4C93'; //lillac
const orange: string = '#fb8500';      //orange
const red: string = '#d00000';

const Pallette: PalleteType = {
  priority: {
    'HIGH': bittersweet,
    'LOW': steelblue,
    'MEDIUM': emerald
  },
  status: {
    'REJECTED': bittersweet,
    'IN_PROGRESS': emerald,
    'COMPLETED': steelblue,
    'CREATED': ultraviolet,
  },
  assigneeGroupType: {
    assigneeOther: steelblue,
    assigneeOverdue: red,
    assigneeStartsToday: orange,
    assigneeCurrentlyWorking: emerald
  },
  teamGroupType: {
    groupOverdue: red,
    groupAvailable: steelblue,
    groupDueSoon: orange
  },
  colors: { red: bittersweet, green: emerald, yellow: sunglow, blue: steelblue, violet: ultraviolet }
};


interface ExtendedInit extends Omit<TasksState, "withGroupBy"> {
  filtered: TaskDescriptor[];
  owners: string[];
  roles: string[];
  pallette: {
    roles: Record<string, string>
    owners: Record<string, string>
    status: Record<string, string>
    priority: Record<string, string>
  }
  profile: Profile
}

class TasksStateBuilder implements TasksMutatorBuilder {
  private _tasks: TaskDescriptor[];
  private _tasksByOwner: Record<string, TaskDescriptor[]>;
  private _filtered: TaskDescriptor[];
  private _groupBy: GroupBy;
  private _groups: Group[];
  private _filterBy: FilterBy[];
  private _searchString: string | undefined;
  private _owners: string[];
  private _roles: string[];
  private _pallette: TasksStatePallette;
  private _profile: Profile;

  constructor(init: ExtendedInit) {
    this._tasks = init.tasks;
    this._tasksByOwner = init.tasksByOwner;
    this._groupBy = init.groupBy;
    this._groups = init.groups;
    this._filterBy = init.filterBy;
    this._searchString = init.searchString;
    this._filtered = init.filtered;
    this._owners = init.owners;
    this._roles = init.roles;
    this._pallette = init.pallette;
    this._profile = init.profile;
  }
  get profile(): Profile { return this._profile }
  get pallette(): TasksStatePallette { return this._pallette }
  get owners(): string[] { return this._owners }
  get roles(): string[] { return this._roles }
  get tasks(): TaskDescriptor[] { return this._tasks }
  get groupBy(): GroupBy { return this._groupBy }
  get groups(): Group[] { return this._groups }
  get filterBy(): FilterBy[] { return this._filterBy }
  get searchString(): string | undefined { return this._searchString }
  get filtered(): TaskDescriptor[] { return this._filtered }
  get tasksByOwner(): Record<string, TaskDescriptor[]> { return this._tasksByOwner }

  withTasks(input: Task[]): TasksStateBuilder {
    const tasks: TaskDescriptor[] = [];
    const roles: string[] = [_nobody_];
    const owners: string[] = [_nobody_];
    const tasksByOwner: Record<string, TaskDescriptor[]> = {};
    const today = new Date(this._profile.today);
    today.setHours(0, 0, 0, 0);

    input.forEach(task => {
      const item = new TaskDescriptorImpl(task, this._profile, today);
      tasks.push(item);

      task.roles.forEach(role => {
        if (!roles.includes(role)) {
          roles.push(role)
        }
      });
      task.assigneeIds.forEach(owner => {
        if (!owners.includes(owner)) {
          owners.push(owner)
        }

        if (!tasksByOwner[owner]) {
          tasksByOwner[owner] = [];
        }
        tasksByOwner[owner].push(item);

      });

      if (task.assigneeIds.length === 0) {
        if (!tasksByOwner[_nobody_]) {
          tasksByOwner[_nobody_] = [];
        }
        tasksByOwner[_nobody_].push(item);
      }

    });

    owners.sort();
    roles.sort();

    const grouping = new GroupVisitor({ groupBy: this._groupBy, roles: this._roles, owners: this._owners });
    const filtered: TaskDescriptor[] = [];
    for (const value of tasks) {
      if (!applyDescFilters(value, this._filterBy)) {
        continue;
      }
      filtered.push(value);
      grouping.visit(value);
    }

    const pallette: TasksStatePallette = {
      roles: {},
      owners: {},
      status: Pallette.status,
      priority: Pallette.priority
    }
    withColors(roles).forEach(e => pallette.roles[e.value] = e.color);
    withColors(owners).forEach(e => pallette.owners[e.value] = e.color);

    return new TasksStateBuilder({
      ...this.clone(),
      groupBy: this._groupBy,
      roles, owners,
      pallette,
      tasks,
      filtered,
      groups: grouping.build(),
      tasksByOwner
    });
  }
  withSearchString(searchString: string): TasksMutatorBuilder {
    const cleaned = searchString.toLowerCase();
    const grouping = new GroupVisitor({ groupBy: this._groupBy, roles: this._roles, owners: this._owners });
    const filtered: TaskDescriptor[] = [];
    for (const value of this._tasks) {
      if (!applyDescFilters(value, this._filterBy)) {
        continue;
      }
      if (!applySearchString(value, cleaned)) {
        continue;
      }
      filtered.push(value);
      grouping.visit(value);
    }
    return new TasksStateBuilder({ ...this.clone(), filterBy: this._filterBy, filtered, groups: grouping.build(), searchString: cleaned });
  }
  withGroupBy(groupBy: GroupBy): TasksStateBuilder {
    const grouping = new GroupVisitor({ groupBy, roles: this._roles, owners: this._owners });
    this._filtered.forEach(value => grouping.visit(value))
    return new TasksStateBuilder({ ...this.clone(), groupBy, groups: grouping.build() });
  }
  withFilterByStatus(status: TaskStatus[]): TasksStateBuilder {
    return this.withFilterBy({ type: 'FilterByStatus', status, disabled: false });
  }
  withFilterByPriority(priority: TaskPriority[]): TasksStateBuilder {
    return this.withFilterBy({ type: 'FilterByPriority', priority, disabled: false });
  }
  withFilterByOwner(owners: string[]): TasksStateBuilder {
    return this.withFilterBy({ type: 'FilterByOwners', owners, disabled: false });
  }
  withFilterByRoles(roles: string[]): TasksStateBuilder {
    return this.withFilterBy({ type: 'FilterByRoles', roles, disabled: false });
  }
  withProfile(profile: Profile): TasksStateBuilder {
    return new TasksStateBuilder({ ...this.clone(), profile });
  }
  withoutFilters(): TasksStateBuilder {
    return this.withFilterBy(undefined);
  }
  clone(): ExtendedInit {
    const init = this;
    return {
      profile: init.profile,
      tasks: init.tasks,
      tasksByOwner: init.tasksByOwner,
      groupBy: init.groupBy,
      groups: init.groups,
      filterBy: init.filterBy,
      searchString: init.searchString,
      filtered: init.filtered,
      owners: init.owners,
      roles: init.roles,
      pallette: init.pallette,
    }
  }


  private withFilterBy(input: FilterBy | undefined): TasksStateBuilder {
    const filterBy = this.createFilters(input);
    const grouping = new GroupVisitor({ groupBy: this._groupBy, roles: this._roles, owners: this._owners });
    const filtered: TaskDescriptor[] = [];
    for (const value of this._tasks) {
      if (!applyDescFilters(value, filterBy)) {
        continue;
      }
      filtered.push(value);
      grouping.visit(value);
    }
    return new TasksStateBuilder({ ...this.clone(), filterBy, filtered, groups: grouping.build() });
  }

  private createFilters(input: FilterBy | undefined): FilterBy[] {
    if (!input) {
      return [];
    }

    let filter = this._filterBy.find(v => v.type === input.type);
    // not created
    if (!filter) {
      return [...this._filterBy, input];
    }

    const result: FilterBy[] = [];
    for (const v of this._filterBy) {
      if (v.type === input.type) {
        const merged = this.mergeFilters(v, input);
        if (merged) {
          result.push(merged);
        }
      } else {
        result.push(v);
      }
    }
    return result;
  }

  private mergeFilters(previous: FilterBy, next: FilterBy): FilterBy | undefined {
    switch (previous.type) {
      case 'FilterByOwners': {
        const a = previous as FilterByOwners;
        const b = next as FilterByOwners;
        const merged: FilterByOwners = {
          disabled: b.disabled,
          type: 'FilterByOwners',
          owners: filterItems(a.owners, b.owners),
        };
        return merged.owners.length === 0 ? undefined : merged;
      }
      case 'FilterByRoles': {
        const a = previous as FilterByRoles;
        const b = next as FilterByRoles;
        const merged: FilterByRoles = {
          disabled: b.disabled,
          type: 'FilterByRoles',
          roles: filterItems(a.roles, b.roles),
        };
        return merged.roles.length === 0 ? undefined : merged;
      }
      case 'FilterByPriority': {
        const a = previous as FilterByPriority;
        const b = next as FilterByPriority;
        const merged: FilterByPriority = {
          disabled: b.disabled,
          type: 'FilterByPriority',
          priority: filterItems(a.priority, b.priority),
        };
        return merged.priority.length === 0 ? undefined : merged;
      }
      case 'FilterByStatus': {
        const a = previous as FilterByStatus;
        const b = next as FilterByStatus;
        const merged: FilterByStatus = {
          disabled: b.disabled,
          type: 'FilterByStatus',
          status: filterItems(a.status, b.status),
        };
        return merged.status.length === 0 ? undefined : merged;
      }
    }
  }
}

function filterItems<T>(previous: T[], next: T[]) {
  const result: T[] = [];
  for (const item of previous) {
    if (next.includes(item)) {
      continue;
    } else {
      result.push(item);
    }
  }

  for (const item of next) {
    if (previous.includes(item)) {
      continue;
    } else {
      result.push(item);
    }
  }
  return result;
}

class GroupVisitor {
  private _groupBy: GroupBy;
  private _groups: Record<string, Group>;
  constructor(init: {
    groupBy: GroupBy;
    roles: string[];
    owners: string[];
  }) {
    this._groupBy = init.groupBy;
    this._groups = {};

    if (init.groupBy === 'none') {
      this._groups[init.groupBy] = { records: [], color: '', id: init.groupBy, type: init.groupBy }
    } else if (init.groupBy === 'owners') {
      withColors(init.owners).forEach(o => this._groups[o.value] = { records: [], color: o.color, id: o.value, type: init.groupBy })
    } else if (init.groupBy === 'roles') {
      withColors(init.roles).forEach(o => this._groups[o.value] = { records: [], color: o.color, id: o.value, type: init.groupBy })
    } else if (init.groupBy === 'priority') {
      const values: TaskPriority[] = ['HIGH', 'LOW', 'MEDIUM'];
      values.forEach(o => this._groups[o] = { records: [], color: Pallette.priority[o], id: o, type: init.groupBy })
    } else if (init.groupBy === 'status') {
      const values: TaskStatus[] = ['CREATED', 'IN_PROGRESS', 'COMPLETED', 'REJECTED'];
      values.forEach(o => this._groups[o] = { records: [], color: Pallette.status[o], id: o, type: init.groupBy })
    } else if (init.groupBy === 'assignee') {
      const values: AssigneeGroupType[] = ['assigneeOverdue', 'assigneeOther', 'assigneeStartsToday', 'assigneeCurrentlyWorking'];
      values.forEach(o => this._groups[o] = { records: [], color: Pallette.assigneeGroupType[o], id: o, type: init.groupBy })
    } else if (init.groupBy === 'team') {
      const values: TeamGroupType[] = ['groupOverdue', 'groupAvailable', 'groupDueSoon'];
      values.forEach(o => this._groups[o] = { records: [], color: Pallette.teamGroupType[o], id: o, type: init.groupBy })
    }

  }

  public build(): Group[] {
    return Object.values(this._groups);
  }

  public visit(task: TaskDescriptor) {
    if (this._groupBy === 'none') {
      this._groups[this._groupBy].records.push(task);
    } else if (this._groupBy === 'owners') {
      if (task.assignees.length) {
        task.assignees.forEach(o => this._groups[o].records.push(task));
      } else {
        this._groups[_nobody_].records.push(task);
      }
    } else if (this._groupBy === 'roles') {
      if (task.roles.length) {
        task.roles.forEach(o => this._groups[o].records.push(task));
      } else {
        this._groups[_nobody_].records.push(task);
      }
    } else if (this._groupBy === 'status') {
      this._groups[task.status].records.push(task);
    } else if (this._groupBy === 'priority') {
      this._groups[task.priority].records.push(task);

    } else if (this._groupBy === 'assignee') {
      const isCompletedOrRejected: boolean = task.status === 'COMPLETED' || task.status === 'REJECTED';
      if (isCompletedOrRejected) {
        return;
      }
      // Include only logged in user tasks
      if (task.assigneeGroupType) {
        this._groups[task.assigneeGroupType].records.push(task);
      }
    } else if (this._groupBy === 'team') {
      const isCompletedOrRejected: boolean = task.status === 'COMPLETED' || task.status === 'REJECTED';
      if (isCompletedOrRejected) {
        return;
      }

      if (task.teamGroupType) {
        this._groups[task.teamGroupType].records.push(task);
      }
    }
  }
}

class TaskDescriptorImpl implements TaskDescriptor {
  private _entry: Task;
  private _created: Date;
  private _dialobId: string;
  private _dueDate: Date | undefined;
  private _startDate: Date | undefined;
  private _uploads: TaskExtension[];
  private _rolesAvatars: AvatarCode[];
  private _ownersAvatars: AvatarCode[];
  private _myWorkType: AssigneeGroupType | undefined;
  private _teamspaceType: TeamGroupType | undefined;

  constructor(entry: Task, profile: Profile, today: Date) {
    this._entry = entry;
    this._created = new Date(entry.created);
    this._startDate = entry.startDate ? new Date(entry.startDate) : undefined;
    this._dueDate = entry.dueDate ? new Date(entry.dueDate) : undefined;
    this._dialobId = entry.extensions.find(t => t.type === 'dialob')!.body;
    this._uploads = entry.extensions.filter(t => t.type === 'upload');
    this._rolesAvatars = getAvatar(entry.roles);
    this._ownersAvatars = getAvatar(entry.assigneeIds);
    this._myWorkType = getMyWorkType(entry, profile, today);
    this._teamspaceType = getTeamspaceType(entry, profile, today);
  }

  get transactions() { return this._entry.transactions }
  get assigneeGroupType() { return this._myWorkType }
  get teamGroupType() { return this._teamspaceType }
  get id() { return this._entry.id }
  get dialobId() { return this._dialobId }
  get entry() { return this._entry }
  get created() { return this._created }
  get dueDate() { return this._dueDate }
  get startDate() { return this._startDate }
  get checklist() { return this._entry.checklist }

  get comments() { return this._entry.comments }
  get status() { return this._entry.status }
  get priority() { return this._entry.priority }
  get roles() { return this._entry.roles }
  get assignees() { return this._entry.assigneeIds }
  get labels() { return this._entry.labels }
  get title() { return this._entry.title }
  get description() { return this._entry.description }
  get uploads() { return this._uploads }
  get rolesAvatars() { return this._rolesAvatars }
  get assigneesAvatars() { return this._ownersAvatars }
}

function getTeamspaceType(task: Task, profile: Profile, today: Date): TeamGroupType | undefined {
  if (profile.roles.filter((role) => task.roles.includes(role)).length === 0) {
    return undefined;
  }

  const { dueDate } = task;
  if (!dueDate) {
    return undefined;
  }

  const dueDateClean = parseISO(dueDate);

  if (isAfter(today, dueDateClean)) {
    return "groupOverdue";
  }
  if (dueDateClean && differenceInCalendarDays(dueDateClean, today) <= 5) {
    return "groupDueSoon";
  }
  return "groupAvailable";
}


function getMyWorkType(task: Task, profile: Profile, today: Date): AssigneeGroupType | undefined {
  if (!task.assigneeIds.includes(profile.userId)) {
    return undefined;
  }

  const { startDate, dueDate } = task;

  if (dueDate && isAfter(today, parseISO(dueDate))) {
    return "assigneeOverdue";
  }
  if (startDate && isEqual(parseISO(startDate), today)) {
    return "assigneeStartsToday";
  }
  if(task.status === 'IN_PROGRESS'){
    return "assigneeCurrentlyWorking";
    
  }
  return "assigneeOther";
}

function getAvatar(values: string[]): { twoletters: string, value: string }[] {
  return values.map(role => {
    const words: string[] = role.replaceAll("-", " ").replaceAll("_", " ").replace(/([A-Z])/g, ' $1').replaceAll("  ", " ").trim().split(" ");

    const result: string[] = [];
    for (const word of words) {
      if (result.length >= 2) {
        break;
      }

      if (word && word.length) {
        const firstLetter = word.substring(0, 1);
        result.push(firstLetter.toUpperCase());
      }
    }
    return { twoletters: result.join(""), value: role };
  });
}

function applyDescFilters(desc: TaskDescriptor, filters: FilterBy[]): boolean {
  for (const filter of filters) {
    if (filter.disabled) {
      continue;
    }
    if (!applyDescFilter(desc, filter)) {
      return false;
    }
  }

  return true;
}

function applySearchString(desc: TaskDescriptor, searchString: string): boolean {
  const description: boolean = desc.description?.toLowerCase().indexOf(searchString) > -1;
  return desc.title.toLowerCase().indexOf(searchString) > -1 || description;
}

function applyDescFilter(desc: TaskDescriptor, filter: FilterBy): boolean {
  switch (filter.type) {
    case 'FilterByOwners': {
      for (const owner of filter.owners) {
        if (desc.assignees.length === 0 && owner === _nobody_) {
          continue;
        }
        if (!desc.assignees.includes(owner)) {
          return false;
        }
      }
      return true;
    }
    case 'FilterByRoles': {
      for (const role of filter.roles) {
        if (desc.roles.length === 0 && role === _nobody_) {
          continue;
        }
        if (!desc.roles.includes(role)) {
          return false;
        }
      }
      return true;
    }
    case 'FilterByStatus': {
      return filter.status.includes(desc.status);
    }
    case 'FilterByPriority': {
      return filter.priority.includes(desc.priority);
    }
  }
  // @ts-ignore
  throw new Error("unknow filter" + filter)
}

function withColors<T>(input: T[]): { color: string, value: T }[] {
  const result: { color: string, value: T }[] = [];
  const colors = Object.values(Pallette.colors);
  let index = 0;
  for (const value of input) {
    result.push({ value, color: colors[index] })
    if (colors.length - 1 === index) {
      index = 0;
    } else {
      index++;
    }
  }

  return result;
}


export { TaskDescriptorImpl, TasksStateBuilder, Pallette, _nobody_ };
export type { };
