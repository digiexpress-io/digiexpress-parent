import { Profile, Tenant, TenantEntry } from 'client';


import { TenantDescriptor, TenantEntryDescriptor, GroupBy, Group, TenantGroupsAndFilters, Data } from './types';
import { applySearchString, withColors } from './util';

interface ExtendedInit extends Omit<TenantGroupsAndFilters,
  "withEntries" |
  "withGroupBy" |
  "withSearchString" |
  "withData"
> {

  filtered: TenantEntryDescriptor[];
  data: Data;
}


class TenantGroupsAndFiltersImpl implements TenantGroupsAndFilters {
  private _data: Data;
  private _filtered: TenantEntryDescriptor[];
  private _groups: Group[];
  private _groupBy: GroupBy;
  private _searchString: string | undefined;

  constructor(init: ExtendedInit) {
    this._data = init.data;
    this._groups = init.groups;
    this._groupBy = init.groupBy;
    this._searchString = init.searchString;
    this._filtered = init.filtered;
  }
  get groups(): Group[] { return this._groups }
  get groupBy(): GroupBy { return this._groupBy }
  get filtered(): TenantEntryDescriptor[] { return this._filtered }
  get searchString(): string | undefined { return this._searchString }

  withGroupBy(groupBy: GroupBy): TenantGroupsAndFiltersImpl {
    const grouping = new GroupVisitor({ groupBy: this._groupBy });
    this._filtered.forEach(value => grouping.visit(value))
    return new TenantGroupsAndFiltersImpl({ ...this.clone(), groupBy, groups: grouping.build() });
  }
  withEntries(input: TenantEntryDescriptor[]): TenantGroupsAndFiltersImpl {
    return this.withData({ tenantEntries: input, profile: this._data.profile, palette: this._data.palette });
  }
  withData(input: Data): TenantGroupsAndFiltersImpl {
    console.log(input);
    const today = new Date(input.profile.today);
    today.setHours(0, 0, 0, 0);
    const cleaned = (this._searchString ?? '').toLowerCase();

    const grouping = new GroupVisitor({ groupBy: this._groupBy });
    const filtered: TenantEntryDescriptor[] = [];
    for (const value of input.tenantEntries) {
      if (this._searchString && !applySearchString(value, cleaned)) {
        continue;
      }
      filtered.push(value);
      grouping.visit(value);
    }

    return new TenantGroupsAndFiltersImpl({ ...this.clone(), filtered, groups: grouping.build(), searchString: cleaned });
  }

  withSearchString(searchString: string): TenantGroupsAndFiltersImpl {
    const cleaned = searchString.toLowerCase();
    const grouping = new GroupVisitor({ groupBy: this._groupBy });
    const filtered: TenantEntryDescriptor[] = [];
    for (const value of this._data.tenantEntries) {
      if (!applySearchString(value, cleaned)) {
        continue;
      }
      filtered.push(value);
      grouping.visit(value);
    }
    return new TenantGroupsAndFiltersImpl({ ...this.clone(), filtered, groups: grouping.build(), searchString: cleaned });
  }
  clone(): ExtendedInit {
    const init = this;
    return {
      data: init._data,
      groupBy: init._groupBy,
      groups: this._groups,
      filtered: this._filtered,
      searchString: this._searchString,
    }
  }
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
    }
  }

  public build(): Group[] {
    return Object.values(this._groups);
  }

  public visit(task: TenantEntryDescriptor) {
    if (this._groupBy === 'none') {
      this._groups[this._groupBy].records.push(task);
    }
  }
}


/**
 * 
 */
class TenantEntryDescriptorImpl implements TenantEntryDescriptor {
  private _source: TenantEntry;
  private _profile: Profile;
  private _created: Date;
  private _lastSaved: Date;

  constructor(source: TenantEntry, profile: Profile, today: Date) {
    this._source = source;
    this._profile = profile;
    this._created = new Date(source.metadata.created);
    this._lastSaved = new Date(source.metadata.lastSaved);

  }
  get source(): TenantEntry { return this._source }
  get formName(): string { return this.source.id } //TODO WHICH IS LABEL AND WHICH IS NAME AND WHICH IS NAME
  get formTitle(): string { return this._source.metadata.label }
  get created(): Date { return this._created }
  get lastSaved(): Date { return this._lastSaved }
  get profile() { return this._profile }
}

/**
 * 
 */
class TenantDescriptorImpl implements TenantDescriptor {
  private _source: Tenant;
  private _profile: Profile;

  constructor(source: Tenant, profile: Profile, today: Date) {
    this._source = source;
    this._profile = profile;
  }

  get profile() { return this._profile }
  get source() { return this._source }

}

export { TenantEntryDescriptorImpl, TenantDescriptorImpl, TenantGroupsAndFiltersImpl };
