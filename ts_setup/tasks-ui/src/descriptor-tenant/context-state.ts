import { Profile, TenantId, FormTechnicalName, Tenant, TenantEntry } from 'client';

import { TenantState, TenantDescriptor, TenantEntryDescriptor, TenantPaletteType } from './types';
import { TenantEntryDescriptorImpl, TenantDescriptorImpl, TenantGroupsAndFiltersImpl } from './types-impl';



interface ExtendedInit extends Omit<TenantState,
  "withProfile" |
  "withTenants" |
  "withActiveTenant" |
  "withTenantEntries" |
  "withActiveTenantEntry" |
  "toGroupsAndFilters"
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

  constructor(init: ExtendedInit) {
    this._tenants = init.tenants;
    this._profile = init.profile;
    this._palette = init.palette;
    this._tenantEntries = init.tenantEntries;
  }


  get tenants(): TenantDescriptor[] { return this._tenants }
  get tenantEntries(): TenantEntryDescriptor[] { return this._tenantEntries }
  get activeTenant(): TenantId | undefined { return this._activeTenant }
  get activeTenantEntry(): FormTechnicalName | undefined { return this._activeTenantEntry }
  get palette(): TenantPaletteType { return this._palette }
  get profile(): Profile { return this._profile }

  toGroupsAndFilters(): TenantGroupsAndFiltersImpl {
    return new TenantGroupsAndFiltersImpl({
      data: this,
      filtered: this._tenantEntries,
      groupBy: 'none',
      groups: [],
      searchString: undefined,
    });
  }

  withTenants(input: Tenant[]): TenantStateBuilder {
    const tenants: TenantDescriptor[] = [];
    const today = new Date(this._profile.today);
    today.setHours(0, 0, 0, 0);

    input.forEach(entry => {
      const item = new TenantDescriptorImpl(entry, this._profile, today);
      tenants.push(item);
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
    const today = new Date(this._profile.today);
    today.setHours(0, 0, 0, 0);
    return new TenantStateBuilder({ ...this.clone(), tenantEntries: entries.map((entry) => new TenantEntryDescriptorImpl(entry, this._profile, today)) });
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
    }
  }
}


export { TenantStateBuilder };
