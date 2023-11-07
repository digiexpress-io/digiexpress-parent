import { Profile } from 'client';

import { TenantState } from './tenant-ctx-types';
import { TenantDescriptor, TenantEntryDescriptor, TenantPaletteType, TenantId, FormTechnicalName, Tenant, TenantEntry } from './descriptor-types';
//import { TaskDescriptorImpl, DescriptorStateImpl } from './descriptor-impl';


interface ExtendedInit extends Omit<TenantState,
  "withProfile" |
  "withTenants" |
  "withActiveTenant" |
  "withTenantEntries" |
  "withActiveTenantEntry"
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
    return {} as any;
  }

  withActiveTenant(tenantId?: TenantId): TenantStateBuilder {
    return {} as any;
  }
  withActiveTenantEntry(id?: FormTechnicalName): TenantStateBuilder {
    return {} as any;
  }

  withTenantEntries(tenantEntries: TenantEntry[]): TenantStateBuilder {
    return {} as any;
  }

  clone(): ExtendedInit {
    const init = this;
    return {
      profile: init.profile,
      palette: init.palette,
      tenants: init._tenants,
      activeTenant: this._activeTenant,
      tenantEntries: this._tenantEntries,
      activeTenantEntry: this._activeTenantEntry
    }
  }
}

export { TenantStateBuilder };
