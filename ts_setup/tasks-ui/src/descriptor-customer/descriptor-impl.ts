import { Customer, Profile, Task } from 'client';

import {
  CustomerCaseDescriptor, FilterBy, Group, GroupBy, FilterByAssignees, FilterByRoles,
  CustomerPaletteType, Data, DescriptorState
} from './types';
import { _nobody_ } from './constants';
import { applyDescFilters, applySearchString, withColors } from './util';



interface ExtendedInit extends Omit<DescriptorState,
  "withSearchString" |
  "withGroupBy" |
  "withFilterByRoles" |
  "withFilterByAssignees" |
  "withCases"
> {

  filtered: CustomerCaseDescriptor[];
  data: Data;
}


class DescriptorStateImpl implements DescriptorState {
  private _data: Data;
  private _filtered: CustomerCaseDescriptor[];
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
  get palette(): CustomerPaletteType { return this._data.palette }
  get cases(): CustomerCaseDescriptor[] { return this._data.cases }

  get groupBy(): GroupBy { return this._groupBy }
  get groups(): Group[] { return this._groups }
  get filterBy(): FilterBy[] { return this._filterBy }
  get searchString(): string | undefined { return this._searchString }
  get filtered(): CustomerCaseDescriptor[] { return this._filtered }

  //TOOD   ???
  withCases(input: Data): DescriptorState {
    const grouping = new GroupVisitor({ groupBy: this._groupBy, roles: this._data.roles, assignees: this._data.assignees });
    const filtered: CustomerCaseDescriptor[] = [];
    for (const value of input.cases) {
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
    const grouping = new GroupVisitor({ groupBy: this._groupBy, roles: this._data.roles, assignees: this._data.assignees });
    const filtered: CustomerCaseDescriptor[] = [];
    for (const value of this._data.cases) {
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
    const grouping = new GroupVisitor({ groupBy, assignees: this._data.assignees, roles: this._data.roles });
    this._filtered.forEach(value => grouping.visit(value))
    return new DescriptorStateImpl({ ...this.clone(), groupBy, groups: grouping.build() });
  }
  withFilterByRoles(roles: string[]): DescriptorState {
    return this.withFilterBy({ type: 'FilterByRoles', roles, disabled: false });
  }
  withFilterByAssignees(assignees: string[]): DescriptorState {
    return this.withFilterBy({ type: 'FilterByAssignees', assignees, disabled: false });
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
      filtered: init.filtered,
    }
  }


  private withFilterBy(input: FilterBy | undefined): DescriptorState {
    const filterBy = this.createFilters(input);
    const grouping = new GroupVisitor({ groupBy: this._groupBy, assignees: this._data.assignees, roles: this._data.roles });
    const filtered: CustomerCaseDescriptor[] = [];
    for (const value of this._data.cases) {
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
      case 'FilterByAssignees': {
        const a = previous as FilterByAssignees;
        const b = next as FilterByAssignees;
        const merged: FilterByAssignees = {
          disabled: b.disabled,
          type: 'FilterByAssignees',
          assignees: filterItems(a.assignees, b.assignees),
        };
        return merged.assignees.length === 0 ? undefined : merged;
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
    assignees: string[];
    roles: string[];
  }) {
    this._groupBy = init.groupBy;
    this._groups = {};

    if (init.groupBy === 'none') {
      this._groups[init.groupBy] = { records: [], color: '', id: init.groupBy, type: init.groupBy }
    } else if (init.groupBy === 'assignees') {
      withColors(init.assignees).forEach(o => this._groups[o.value] = { records: [], color: o.color, id: o.value, type: init.groupBy })
    } else if (init.groupBy === 'roles') {
      withColors(init.assignees).forEach(o => this._groups[o.value] = { records: [], color: o.color, id: o.value, type: init.groupBy })
    }
  }

  public build(): Group[] {
    return Object.values(this._groups);
  }

  public visit(task: CustomerCaseDescriptor) {
    if (this._groupBy === 'none') {
      this._groups[this._groupBy].records.push(task);
    }
  }
}


class CustomerCaseDescriptorImpl implements CustomerCaseDescriptor {
  private _profile: Profile;
  private _entry: Customer;
  private _firstName: string;
  private _lastName: string;
  private _ssn: string;
  private _task: Task;



  constructor(entry: Customer, profile: Profile, today: Date, task: Task) {
    this._entry = entry;
    this._profile = profile;
    this._firstName = entry.firstName;
    this._lastName = entry.lastName;
    this._ssn = entry.ssn;
    this._task = task;

  }
  //get taskTitle() { return this._taskTitle }
  get profile() { return this._profile }
  get entry() { return this._entry }
  get firstName() { return this._firstName }
  get lastName() { return this._lastName }
  get ssn() { return this._ssn }
  get task() { return this._task }
  get id() { return this._entry.id }

}

export { CustomerCaseDescriptorImpl, DescriptorStateImpl };
export type { ExtendedInit };
