import { Profile, TenantId, FormTechnicalName, Tenant, TenantEntry } from 'client';

import { TenantState } from './descriptor-types';
import { TenantDescriptor, TenantEntryDescriptor, TenantPaletteType, Group, GroupBy } from './descriptor-types';
import { applySearchString } from './descriptor-util';
//import { TaskDescriptorImpl, DescriptorStateImpl } from './descriptor-impl';


interface ExtendedInit extends Omit<TenantState,
  "withProfile" |
  "withTenants" |
  "withActiveTenant" |
  "withTenantEntries" |
  "withActiveTenantEntry" |
  "withSearchString"
> {
  palette: {};
  profile: Profile;
}

class TenantStateBuilder implements TenantState {
  private _tenants: TenantDescriptor[];
  private _tenantEntries: TenantEntryDescriptor[];
  private _activeTenant: TenantId | undefined;
  private _activeTenantEntry: FormTechnicalName | undefined;
  private _palette: TenantPaletteType;
  private _profile: Profile;
  private _groups: Group[];
  private _groupBy: GroupBy;
  private _filtered: TenantEntryDescriptor[];
  private _searchString: string | undefined;


  constructor(init: ExtendedInit) {
    this._tenants = init.tenants;
    this._profile = init.profile;
    this._palette = init.palette;
    this._tenantEntries = init.tenantEntries;
    this._groups = init.groups;
    this._groupBy = init.groupBy;
    this._filtered = init.filtered;
    this._searchString = init.searchString
  }
  get tenants(): TenantDescriptor[] { return this._tenants }
  get tenantEntries(): TenantEntryDescriptor[] { return this._tenantEntries }
  get activeTenant(): TenantId | undefined { return this._activeTenant }
  get activeTenantEntry(): FormTechnicalName | undefined { return this._activeTenantEntry }
  get palette(): TenantPaletteType { return this._palette }
  get profile(): Profile { return this._profile }
  get groups(): Group[] { return this._groups }
  get groupBy(): GroupBy { return this._groupBy }
  get filtered(): TenantEntryDescriptor[] { return this._filtered }
  get searchString(): string | undefined { return this._searchString }


  withTenants(input: Tenant[]): TenantStateBuilder {
    const tenants: TenantDescriptor[] = [];
    const profile = {}
    const today = new Date(this._profile.today);
    today.setHours(0, 0, 0, 0);

    input.forEach(entry => {
      //const item = new TaskDescriptorImpl(task, this._profile, today);
      //entries.push(item);
    });

    const palette: TenantPaletteType = {}

    return new TenantStateBuilder({
      ...this.clone(),
      tenants,
      palette
    });
  }

  withProfile(profile: Profile): TenantStateBuilder {
    return new TenantStateBuilder({ ...this.clone(), profile })
  }
  withActiveTenant(tenantId?: TenantId): TenantStateBuilder {
    return new TenantStateBuilder({ ...this.clone(), activeTenant: tenantId });
  }
  withActiveTenantEntry(id?: FormTechnicalName): TenantStateBuilder {
    return new TenantStateBuilder({ ...this.clone(), activeTenantEntry: id });
  }
  withTenantEntries(entries: TenantEntry[]): TenantStateBuilder {

    //TODO TenantEntryDescriptorImpl
    return new TenantStateBuilder({ ...this.clone(), tenantEntries: [] });
  }
  withSearchString(searchString: string): TenantStateBuilder {
    const cleaned = searchString.toLowerCase();
    const grouping = new GroupVisitor({ groupBy: this._groupBy });
    const filtered: TenantEntryDescriptor[] = [];
    for (const value of this._tenantEntries) {
      if (!applySearchString(value, cleaned)) {
        continue;
      }
      filtered.push(value);
      grouping.visit(value);
    }
    return new TenantStateBuilder({ ...this.clone(), filtered, groups: grouping.build(), searchString: cleaned });
  }


  clone(): ExtendedInit {
    const init = this;
    return {
      profile: init.profile,
      palette: init.palette,
      tenants: init._tenants,
      activeTenant: this._activeTenant,
      tenantEntries: this._tenantEntries,
      activeTenantEntry: this._activeTenantEntry,
      groups: this._groups,
      groupBy: this._groupBy,
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

export { TenantStateBuilder };
