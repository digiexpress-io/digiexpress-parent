import { TaskPriority, TaskStatus } from 'client';

import {
  TaskDescriptor, FilterBy, Group, GroupBy,
  FilterByOwners, FilterByPriority, FilterByRoles, FilterByStatus, AssigneeGroupType, TeamGroupType,
  TaskGroupsAndFilters
} from './types';
import { _nobody_, Palette } from './constants';
import { applyDescFilters, applySearchString  } from './util';


interface ExtendedInit extends Omit<TaskGroupsAndFilters,
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
  data: readonly TaskDescriptor[];
}

class TaskGroupsAndFiltersImpl implements TaskGroupsAndFilters {
  private _data: readonly TaskDescriptor[];
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
  get tasks(): readonly TaskDescriptor[] { return this._data }
  get groupBy(): GroupBy { return this._groupBy }
  get groups(): Group[] { return this._groups }
  get filterBy(): FilterBy[] { return this._filterBy }
  get searchString(): string | undefined { return this._searchString }
  get filtered(): TaskDescriptor[] { return this._filtered }

  withTasks(input: readonly TaskDescriptor[]): TaskGroupsAndFiltersImpl {
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const grouping = new GroupVisitor({ groupBy: this._groupBy });
    const filtered: TaskDescriptor[] = [];
    for (const value of input) {
      if (!applyDescFilters(value, this._filterBy)) {
        continue;
      }
      filtered.push(value);
      grouping.visit(value);
    }

    return new TaskGroupsAndFiltersImpl({
      ...this.clone(),
      data: input,
      groupBy: this._groupBy,
      filtered,
      groups: grouping.build(),
    });
  }
  withSearchString(searchString: string): TaskGroupsAndFiltersImpl {
    const cleaned = searchString.toLowerCase();
    const grouping = new GroupVisitor({ groupBy: this._groupBy });
    const filtered: TaskDescriptor[] = [];
    for (const value of this._data) {
      if (!applyDescFilters(value, this._filterBy)) {
        continue;
      }
      if (!applySearchString(value, cleaned)) {
        continue;
      }
      filtered.push(value);
      grouping.visit(value);
    }
    return new TaskGroupsAndFiltersImpl({ ...this.clone(), filterBy: this._filterBy, filtered, groups: grouping.build(), searchString: cleaned });
  }
  withGroupBy(groupBy: GroupBy): TaskGroupsAndFiltersImpl {
    const grouping = new GroupVisitor({ groupBy });
    this._filtered.forEach(value => grouping.visit(value))
    return new TaskGroupsAndFiltersImpl({ ...this.clone(), groupBy, groups: grouping.build() });
  }
  withFilterByStatus(status: TaskStatus[]): TaskGroupsAndFiltersImpl {
    return this.withFilterBy({ type: 'FilterByStatus', status, disabled: false });
  }
  withFilterByPriority(priority: TaskPriority[]): TaskGroupsAndFiltersImpl {
    return this.withFilterBy({ type: 'FilterByPriority', priority, disabled: false });
  }
  withFilterByOwner(owners: string[]): TaskGroupsAndFiltersImpl {
    return this.withFilterBy({ type: 'FilterByOwners', owners, disabled: false });
  }
  withFilterByRoles(roles: string[]): TaskGroupsAndFiltersImpl {
    return this.withFilterBy({ type: 'FilterByRoles', roles, disabled: false });
  }
  withoutFilters(): TaskGroupsAndFiltersImpl {
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


  private withFilterBy(input: FilterBy | undefined): TaskGroupsAndFiltersImpl {
    const filterBy = this.createFilters(input);
    const grouping = new GroupVisitor({ groupBy: this._groupBy });
    const filtered: TaskDescriptor[] = [];
    for (const value of this._data) {
      if (!applyDescFilters(value, filterBy)) {
        continue;
      }
      filtered.push(value);
      grouping.visit(value);
    }
    return new TaskGroupsAndFiltersImpl({ ...this.clone(), filterBy, filtered, groups: grouping.build() });
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
  }) {
    this._groupBy = init.groupBy;
    this._groups = {};

    if (init.groupBy === 'none') {
      this._groups[init.groupBy] = { records: [], color: '', id: init.groupBy, type: init.groupBy }
    } else if (init.groupBy === 'owners') {
      /// TODO withColors(init.owners).forEach(o => this._groups[o.value] = { records: [], color: o.color, id: o.value, type: init.groupBy })
    } else if (init.groupBy === 'roles') {
      // TODO withColors(init.roles).forEach(o => this._groups[o.value] = { records: [], color: o.color, id: o.value, type: init.groupBy })
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



export { TaskGroupsAndFiltersImpl };
export type { ExtendedInit };
