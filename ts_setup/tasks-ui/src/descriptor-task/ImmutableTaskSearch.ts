import { TaskPriority, TaskStatus } from 'client';
import { TaskDescriptor, _nobody_ } from './types';


export type FilterByStatus = { type: 'FilterByStatus', status: TaskStatus[], disabled: boolean }
export type FilterByPriority = { type: 'FilterByPriority', priority: TaskPriority[], disabled: boolean }
export type FilterByOwners = { type: 'FilterByOwners', owners: string[], disabled: boolean }
export type FilterByRoles = { type: 'FilterByRoles', roles: string[], disabled: boolean }
export type FilterBy = FilterByStatus | FilterByPriority | FilterByOwners | FilterByRoles;


export interface TaskSearch {
  data: readonly TaskDescriptor[];
  filtered: readonly TaskDescriptor[];
  filterBy: readonly FilterBy[];
  searchString?: string | undefined;
}


export interface TaskSearchInit {
  data: readonly TaskDescriptor[];
  filtered?: readonly TaskDescriptor[];
  filterBy?: readonly FilterBy[];
  searchString?: string | undefined;
}

export interface TaskSearchReducers {
  withData(input: readonly TaskDescriptor[]): ImmutableTaskSearch;
  withSearchString(searchString: string): ImmutableTaskSearch;
  withFilterByStatus(status: TaskStatus[]): ImmutableTaskSearch
  withFilterByPriority(priority: TaskPriority[]): ImmutableTaskSearch
  withFilterByOwner(owners: string[]): ImmutableTaskSearch
  withFilterByRoles(roles: string[]): ImmutableTaskSearch
  withoutFilters(): ImmutableTaskSearch;
}


export class ImmutableTaskSearch implements TaskSearch, TaskSearchReducers {
  private _data: readonly TaskDescriptor[];
  private _filtered: readonly TaskDescriptor[];
  private _filterBy: readonly FilterBy[];
  private _searchString: string | undefined;

  constructor(init: TaskSearchInit) {
    this._data = init.data;
    this._searchString = init.searchString;
    this._filterBy = init.filterBy ?? [];
    this._filtered = init.filtered ?? this.initFiltered(init);
  }
  get data(): readonly TaskDescriptor[] { return this._data }
  get filterBy(): readonly FilterBy[] { return this._filterBy }
  get searchString(): string | undefined { return this._searchString }
  get filtered(): readonly TaskDescriptor[] { return this._filtered }

  initFiltered(init: TaskSearchInit): readonly TaskDescriptor[] {
    if(!init.searchString) {
      return init.data;
    }

    const cleaned = init.searchString.toLowerCase();
    const filtered: TaskDescriptor[] = [];
    for (const value of this._data) {
      if (!applyDescFilters(value, this._filterBy)) {
        continue;
      }
      if (!applySearchString(value, cleaned)) {
        continue;
      }
      filtered.push(value);
    }
    return Object.freeze(filtered);
  }

  withData(input: readonly TaskDescriptor[]): ImmutableTaskSearch {
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const filtered: TaskDescriptor[] = [];
    for (const value of input) {
      if (!applyDescFilters(value, this._filterBy)) {
        continue;
      }
      filtered.push(value);
    }

    return new ImmutableTaskSearch({ ...this.clone(), data: input, filtered });
  }
  withSearchString(searchString: string): ImmutableTaskSearch {
    const cleaned = searchString.toLowerCase();
    const filtered: TaskDescriptor[] = [];
    for (const value of this._data) {
      if (!applyDescFilters(value, this._filterBy)) {
        continue;
      }
      if (!applySearchString(value, cleaned)) {
        continue;
      }
      filtered.push(value);
    }
    return new ImmutableTaskSearch({ ...this.clone(), filterBy: this._filterBy, filtered, searchString: cleaned });
  }
  withFilterByStatus(status: TaskStatus[]): ImmutableTaskSearch {
    return this.withFilterBy({ type: 'FilterByStatus', status, disabled: false });
  }
  withFilterByPriority(priority: TaskPriority[]): ImmutableTaskSearch {
    return this.withFilterBy({ type: 'FilterByPriority', priority, disabled: false });
  }
  withFilterByOwner(owners: string[]): ImmutableTaskSearch {
    return this.withFilterBy({ type: 'FilterByOwners', owners, disabled: false });
  }
  withFilterByRoles(roles: string[]): ImmutableTaskSearch {
    return this.withFilterBy({ type: 'FilterByRoles', roles, disabled: false });
  }
  withoutFilters(): ImmutableTaskSearch {
    return this.withFilterBy(undefined);
  }
  clone(): TaskSearchInit {
    const init = this;
    return {
      data: init._data,
      filterBy: init.filterBy,
      searchString: init.searchString,
      filtered: init.filtered
    }
  }


  private withFilterBy(input: FilterBy | undefined): ImmutableTaskSearch {
    const filterBy = this.createFilters(input);
    const filtered: TaskDescriptor[] = [];
    for (const value of this._data) {
      if (!applyDescFilters(value, filterBy)) {
        continue;
      }
      filtered.push(value);
    }
    return new ImmutableTaskSearch({ ...this.clone(), filterBy, filtered });
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


function applyDescFilters(desc: TaskDescriptor, filters: readonly FilterBy[]): boolean {
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
