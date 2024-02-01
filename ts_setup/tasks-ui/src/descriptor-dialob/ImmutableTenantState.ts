import { TenantId, Tenant, TenantEntry } from 'client';
import { TenantState, TenantDescriptor, TenantEntryDescriptor } from './types';


export class ImmutableTenantState implements TenantState {
  private _tenants: TenantDescriptor[];
  private _tenantEntries: TenantEntryDescriptor[];
  private _activeTenant: TenantId | undefined;

  constructor(init: TenantState) {
    this._tenants = init.tenants;
    this._tenantEntries = init.tenantEntries;
    this._activeTenant = init.activeTenant;
  }

  get tenants(): TenantDescriptor[] { return this._tenants }
  get tenantEntries(): TenantEntryDescriptor[] { return this._tenantEntries }
  get activeTenant(): TenantId | undefined { return this._activeTenant }
  
  withTenants(input: Tenant[]): ImmutableTenantState {
    const tenants: TenantDescriptor[] = [];
    input.forEach(entry => {
      const item = new TenantDescriptorImpl(entry);
      tenants.push(item);
    });
    return new ImmutableTenantState({ ...this.clone(), tenants });
  }
  withActiveTenant(tenantId?: TenantId): ImmutableTenantState {
    return new ImmutableTenantState({ ...this.clone(), activeTenant: tenantId });
  }
  withTenantEntries(entries: TenantEntry[]): ImmutableTenantState {
    return new ImmutableTenantState({ ...this.clone(), tenantEntries: entries.map((entry) => new TenantEntryDescriptorImpl(entry, this._activeTenant!)) });
  }

  clone(): TenantState {
    const init = this;
    return {
      tenants: init._tenants,
      activeTenant: this._activeTenant,
      tenantEntries: this._tenantEntries
    }
  }
}


class TenantEntryDescriptorImpl implements TenantEntryDescriptor {
  private _source: TenantEntry;
  private _tenantId: string;
  private _created: Date;
  private _lastSaved: Date;

  constructor(source: TenantEntry, tenantId: string) {
    this._source = source;
    this._created = new Date(source.metadata.created);
    this._lastSaved = new Date(source.metadata.lastSaved);
    this._tenantId = tenantId;

  }
  get source(): TenantEntry { return this._source }
  get formName(): string { return this.source.id } //TODO WHICH IS LABEL AND WHICH IS NAME AND WHICH IS NAME
  get formTitle(): string { return this._source.metadata.label }
  get created(): Date { return this._created }
  get lastSaved(): Date { return this._lastSaved }
  get tenantId() { return this._tenantId }
}

/**
 * 
 */
class TenantDescriptorImpl implements TenantDescriptor {
  private _source: Tenant;
  constructor(source: Tenant) {
    this._source = source
  }
  get source() { return this._source }

}