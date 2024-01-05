import { Project, RepoType, UserProfileAndOrg } from 'client';

import {
  ProjectDescriptor, FilterBy, Group, GroupBy,
  FilterByRepoType, FilterByUsers,
  ProjectPaletteType, Data, GroupsAndFilters
} from './types';
import { Palette } from './constants';
import { applyDescFilters, applySearchString } from './util';
import { withColors } from 'components-colors';



interface ExtendedInit extends Omit<GroupsAndFilters,
  "withSearchString" |
  "withProjects" |
  "withGroupBy" |
  "withFilterByRepoType" |
  "withFilterByUsers"
> {

  filtered: ProjectDescriptor[];
  data: Data;
}

class GroupsAndFiltersImpl implements GroupsAndFilters {
  private _data: Data;
  private _filtered: ProjectDescriptor[];
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
  get profile(): UserProfileAndOrg { return this._data.profile }
  get palette(): ProjectPaletteType { return this._data.palette }
  get tasks(): ProjectDescriptor[] { return this._data.projects }

  get groupBy(): GroupBy { return this._groupBy }
  get groups(): Group[] { return this._groups }
  get filterBy(): FilterBy[] { return this._filterBy }
  get searchString(): string | undefined { return this._searchString }
  get filtered(): ProjectDescriptor[] { return this._filtered }

  withProjects(input: Data): GroupsAndFiltersImpl {
    const today = new Date(input.profile.today);
    today.setHours(0, 0, 0, 0);

    const grouping = new GroupVisitor({ groupBy: this._groupBy, users: input.users });
    const filtered: ProjectDescriptor[] = [];
    for (const value of input.projects) {
      if (!applyDescFilters(value, this._filterBy)) {
        continue;
      }
      filtered.push(value);
      grouping.visit(value);
    }

    return new GroupsAndFiltersImpl({
      ...this.clone(),
      data: input,
      groupBy: this._groupBy,
      filtered,
      groups: grouping.build(),
    });
  }
  withSearchString(searchString: string): GroupsAndFiltersImpl {
    const cleaned = searchString.toLowerCase();
    const grouping = new GroupVisitor({ groupBy: this._groupBy, users: this._data.users });
    const filtered: ProjectDescriptor[] = [];
    for (const value of this._data.projects) {
      if (!applyDescFilters(value, this._filterBy)) {
        continue;
      }
      if (!applySearchString(value, cleaned)) {
        continue;
      }
      filtered.push(value);
      grouping.visit(value);
    }
    return new GroupsAndFiltersImpl({ ...this.clone(), filterBy: this._filterBy, filtered, groups: grouping.build(), searchString: cleaned });
  }
  withGroupBy(groupBy: GroupBy): GroupsAndFiltersImpl {
    const grouping = new GroupVisitor({ groupBy, users: this._data.users });
    this._filtered.forEach(value => grouping.visit(value))
    return new GroupsAndFiltersImpl({ ...this.clone(), groupBy, groups: grouping.build() });
  }
  withFilterByRepoType(repoType: RepoType[]): GroupsAndFiltersImpl {
    return this.withFilterBy({ type: 'FilterByRepoType', repoType, disabled: false });
  }
  withFilterByUsers(users: string[]): GroupsAndFiltersImpl {
    return this.withFilterBy({ type: 'FilterByUsers', users, disabled: false });
  }
  withoutFilters(): GroupsAndFiltersImpl {
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


  private withFilterBy(input: FilterBy | undefined): GroupsAndFiltersImpl {
    const filterBy = this.createFilters(input);
    const grouping = new GroupVisitor({ groupBy: this._groupBy, users: this._data.users });
    const filtered: ProjectDescriptor[] = [];
    for (const value of this._data.projects) {
      if (!applyDescFilters(value, filterBy)) {
        continue;
      }
      filtered.push(value);
      grouping.visit(value);
    }
    return new GroupsAndFiltersImpl({ ...this.clone(), filterBy, filtered, groups: grouping.build() });
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
      case 'FilterByUsers': {
        const a = previous as FilterByUsers;
        const b = next as FilterByUsers;
        const merged: FilterByUsers = {
          disabled: b.disabled,
          type: 'FilterByUsers',
          users: filterItems(a.users, b.users),
        };
        return merged.users.length === 0 ? undefined : merged;
      }
      case 'FilterByRepoType': {
        const a = previous as FilterByRepoType;
        const b = next as FilterByRepoType;
        const merged: FilterByRepoType = {
          disabled: b.disabled,
          type: 'FilterByRepoType',
          repoType: filterItems(a.repoType, b.repoType),
        };
        return merged.repoType.length === 0 ? undefined : merged;
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
    users: string[];
  }) {
    this._groupBy = init.groupBy;
    this._groups = {};

    if (init.groupBy === 'none') {
      this._groups[init.groupBy] = { records: [], color: '', id: init.groupBy, type: init.groupBy }
    } else if (init.groupBy === 'users') {
      withColors(init.users).forEach(o => this._groups[o.value] = { records: [], color: o.color, id: o.value, type: init.groupBy })
    } else if (init.groupBy === 'repoType') {
      const values: RepoType[] = ['DIALOB', 'STENCIL', 'WRENCH', 'TASKS', 'CONFIG', 'CRM', 'USER_PROFILE', 'TENANT'];

      // @ts-ignore
      values.forEach(o => this._groups[o] = { records: [], color: Palette.repoType[o] ?? 'red', id: o, type: init.groupBy })
    }
  }

  public build(): Group[] {
    return Object.values(this._groups);
  }

  public visit(task: ProjectDescriptor) {
    if (this._groupBy === 'none') {
      this._groups[this._groupBy].records.push(task);
    }
  }
}

class ProjectDescriptorImpl implements ProjectDescriptor {
  private _entry: Project;
  private _created: Date;
  private _updated: Date;
  private _profile: UserProfileAndOrg;

  constructor(entry: Project, profile: UserProfileAndOrg, today: Date) {
    this._entry = entry;
    this._created = new Date(entry.created);
    this._updated = new Date(entry.updated);;
    this._profile = profile;
  }

  get profile() { return this._profile }
  get id() { return this._entry.id }
  get name() { return this._entry.name }
  get entry() { return this._entry }
  get created() { return this._created }
  get updated() { return this._updated }

}

export { ProjectDescriptorImpl, GroupsAndFiltersImpl };
export type { ExtendedInit };
