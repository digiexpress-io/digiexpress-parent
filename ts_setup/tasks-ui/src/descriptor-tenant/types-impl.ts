import { Profile, Tenant, TenantEntry } from 'client';


import { TenantDescriptor, TenantEntryDescriptor } from './types';

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
  get formName(): string { return this.source.metadata.label } //TODO WHICH IS LABEL AND WHICH IS NAME AND WHICH IS NAME
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

export { TenantEntryDescriptorImpl, TenantDescriptorImpl };
