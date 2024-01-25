import { Customer, UserProfileAndOrg, Person } from 'client';

import { CustomerDescriptor } from './types';



class CustomerDescriptorImpl implements CustomerDescriptor {
  private _profile: UserProfileAndOrg;
  private _entry: Customer;
  private _today: Date;
  private _created: Date;
  private _lastLogin: Date;
  private _displayName: string;

  constructor(entry: Customer, profile: UserProfileAndOrg, today: Date) {
    this._entry = entry;
    this._profile = profile;
    this._today = today;

    const person = this._entry.body as Person;
    this._displayName = person.firstName + " " + person.lastName;
    this._created = new Date(this.entry.created);
    this._lastLogin = new Date(this.entry.created);
  }
  get profile() { return this._profile }
  get entry() { return this._entry }
  get id() { return this._entry.id }

  get displayName() { return this._displayName }
  get customerType() { return this.entry.body.type }
  get tasks(): string[] { return [] }
  get created(): Date { return this._created }
  get lastLogin(): Date { return this._lastLogin }

  toPerson(): Person {
    return this._entry.body as Person;
  }
}

export { CustomerDescriptorImpl };
export type { };
