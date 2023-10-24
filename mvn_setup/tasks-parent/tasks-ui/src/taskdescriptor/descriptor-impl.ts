import { Task, TaskExtension, TaskPriority, TaskStatus } from 'taskclient/task-types';
import {
  TaskDescriptor, FilterBy, Group, GroupBy,
  FilterByOwners, FilterByPriority, FilterByRoles, FilterByStatus, AvatarCode, AssigneeGroupType, TeamGroupType,
  TasksPaletteType, Data
} from './descriptor-types';

import { Profile } from 'taskclient';
import { DescriptorState } from './descriptor-types';
import { _nobody_, Palette } from './descriptor-constants';

import { applyDescFilters, applySearchString, withColors, getDaysUntilDue, getAvatar, getMyWorkType, getTeamspaceType } from './descriptor-util';

interface ExtendedInit extends Omit<DescriptorState,
  "withGroupBy" |
  "withTasks" |
  "withGroupBy" |
  "withSearchString" |
  "withFilterByStatus" |
  "withFilterByPriority" |
  "withFilterByOwner" |
  "withFilterByRoles"
> {

  filtered: TaskDescriptor[];
  data: Data;
}

class DescriptorStateImpl implements DescriptorState {
  private _data: Data;
  private _filtered: TaskDescriptor[];
  private _groupBy: GroupBy;
  private _groups: Group[];
  private _filterBy: FilterBy[];
  private _searchString: string | undefined;

  constructor(init: ExtendedInit) {
    this._data = init.data;
    this._groupBy = init.groupBy;
    this._groups = init.groups;
    this._filterBy = init.filterBy;
    this._searchString = init.searchString;
    this._filtered = init.filtered;
  }
  get profile(): Profile { return this._data.profile }
  get palette(): TasksPaletteType { return this._data.palette }
  get tasks(): TaskDescriptor[] { return this._data.tasks }

  get groupBy(): GroupBy { return this._groupBy }
  get groups(): Group[] { return this._groups }
  get filterBy(): FilterBy[] { return this._filterBy }
  get searchString(): string | undefined { return this._searchString }
  get filtered(): TaskDescriptor[] { return this._filtered }

  withTasks(input: Data): DescriptorState {
    const today = new Date(input.profile.today);
    today.setHours(0, 0, 0, 0);

    const grouping = new GroupVisitor({ groupBy: this._groupBy, roles: input.roles, owners: input.owners });
    const filtered: TaskDescriptor[] = [];
    for (const value of input.tasks) {
      if (!applyDescFilters(value, this._filterBy)) {
        continue;
      }
      filtered.push(value);
      grouping.visit(value);
    }

    return new DescriptorStateImpl({
      ...this.clone(),
      data: input,
      groupBy: this._groupBy,
      filtered,
      groups: grouping.build(),
    });
  }
  withSearchString(searchString: string): DescriptorState {
    const cleaned = searchString.toLowerCase();
    const grouping = new GroupVisitor({ groupBy: this._groupBy, roles: this._data.roles, owners: this._data.owners });
    const filtered: TaskDescriptor[] = [];
    for (const value of this._data.tasks) {
      if (!applyDescFilters(value, this._filterBy)) {
        continue;
      }
      if (!applySearchString(value, cleaned)) {
        continue;
      }
      filtered.push(value);
      grouping.visit(value);
    }
    return new DescriptorStateImpl({ ...this.clone(), filterBy: this._filterBy, filtered, groups: grouping.build(), searchString: cleaned });
  }
  withGroupBy(groupBy: GroupBy): DescriptorState {
    const grouping = new GroupVisitor({ groupBy, roles: this._data.roles, owners: this._data.owners });
    this._filtered.forEach(value => grouping.visit(value))
    return new DescriptorStateImpl({ ...this.clone(), groupBy, groups: grouping.build() });
  }
  withFilterByStatus(status: TaskStatus[]): DescriptorState {
    return this.withFilterBy({ type: 'FilterByStatus', status, disabled: false });
  }
  withFilterByPriority(priority: TaskPriority[]): DescriptorState {
    return this.withFilterBy({ type: 'FilterByPriority', priority, disabled: false });
  }
  withFilterByOwner(owners: string[]): DescriptorState {
    return this.withFilterBy({ type: 'FilterByOwners', owners, disabled: false });
  }
  withFilterByRoles(roles: string[]): DescriptorState {
    return this.withFilterBy({ type: 'FilterByRoles', roles, disabled: false });
  }
  withoutFilters(): DescriptorState {
    return this.withFilterBy(undefined);
  }
  clone(): ExtendedInit {
    const init = this;
    return {
      data: init._data,
      groupBy: init.groupBy,
      groups: init.groups,
      filterBy: init.filterBy,
      searchString: init.searchString,
      filtered: init.filtered
    }
  }


  private withFilterBy(input: FilterBy | undefined): DescriptorState {
    const filterBy = this.createFilters(input);
    const grouping = new GroupVisitor({ groupBy: this._groupBy, roles: this._data.roles, owners: this._data.owners });
    const filtered: TaskDescriptor[] = [];
    for (const value of this._data.tasks) {
      if (!applyDescFilters(value, filterBy)) {
        continue;
      }
      filtered.push(value);
      grouping.visit(value);
    }
    return new DescriptorStateImpl({ ...this.clone(), filterBy, filtered, groups: grouping.build() });
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
      values.forEach(o => this._groups[o] = { records: [], color: Palette.priority[o], id: o, type: init.groupBy })
    } else if (init.groupBy === 'status') {
      const values: TaskStatus[] = ['CREATED', 'IN_PROGRESS', 'COMPLETED', 'REJECTED'];
      values.forEach(o => this._groups[o] = { records: [], color: Palette.status[o], id: o, type: init.groupBy })
    } else if (init.groupBy === 'assignee') {
      const values: AssigneeGroupType[] = ['assigneeOverdue', 'assigneeOther', 'assigneeStartsToday', 'assigneeCurrentlyWorking'];
      values.forEach(o => this._groups[o] = { records: [], color: Palette.assigneeGroupType[o], id: o, type: init.groupBy })
    } else if (init.groupBy === 'team') {
      const values: TeamGroupType[] = ['groupOverdue', 'groupAvailable', 'groupDueSoon'];
      values.forEach(o => this._groups[o] = { records: [], color: Palette.teamGroupType[o], id: o, type: init.groupBy })
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
  private _dialobId: string | undefined;
  private _dueDate: Date | undefined;
  private _startDate: Date | undefined;
  private _daysUntilDue: number | undefined;
  private _uploads: TaskExtension[];
  private _rolesAvatars: AvatarCode[];
  private _ownersAvatars: AvatarCode[];
  private _myWorkType: AssigneeGroupType | undefined;
  private _teamspaceType: TeamGroupType | undefined;
  private _profile: Profile;

  constructor(entry: Task, profile: Profile, today: Date) {
    this._entry = entry;
    this._created = new Date(entry.created);
    this._startDate = entry.startDate ? new Date(entry.startDate) : undefined;
    this._dueDate = entry.dueDate ? new Date(entry.dueDate) : undefined;
    this._daysUntilDue = entry.dueDate ? getDaysUntilDue(entry, today) : undefined;
    this._dialobId = entry.extensions.find(t => t.type === 'dialob')?.body;
    this._uploads = entry.extensions.filter(t => t.type === 'upload');
    this._rolesAvatars = getAvatar(entry.roles);
    this._ownersAvatars = getAvatar(entry.assigneeIds);
    this._myWorkType = getMyWorkType(entry, profile, today);
    this._teamspaceType = getTeamspaceType(entry, profile, today);
    this._profile = profile;
  }

  get transactions() { return this._entry.transactions }
  get assigneeGroupType() { return this._myWorkType }
  get teamGroupType() { return this._teamspaceType }
  get profile() { return this._profile }
  get id() { return this._entry.id }
  get dialobId() { return this._dialobId }
  get entry() { return this._entry }
  get created() { return this._created }
  get dueDate() { return this._dueDate }
  get startDate() { return this._startDate }
  get checklist() { return this._entry.checklist }
  get daysUntilDue() { return this._daysUntilDue }

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




export { TaskDescriptorImpl, DescriptorStateImpl };
export type { ExtendedInit };
