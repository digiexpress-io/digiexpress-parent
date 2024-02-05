
import { CustomerDescriptor, Customer, Person } from './customer-types';


class ImmutableCustomerDescriptor implements CustomerDescriptor {
  private _entry: Customer;
  private _created: Date;
  private _lastLogin: Date;
  private _displayName: string;

  constructor(entry: Customer) {
    this._entry = entry;
    const person = this._entry.body as Person;
    this._displayName = person.firstName + " " + person.lastName;
    this._created = new Date(this.entry.created);
    this._lastLogin = new Date(this.entry.created);
  }
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

export { ImmutableCustomerDescriptor };
export type { };
