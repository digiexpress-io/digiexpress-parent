import { Customer, UserProfile, resolveAvatar, Person } from 'client';

import {
  AvatarCode, CustomerDescriptor
} from './types';



class CustomerDescriptorImpl implements CustomerDescriptor {
  private _profile: UserProfile;
  private _entry: Customer;
  private _avatar: AvatarCode;
  private _today: Date;

  constructor(entry: Customer, profile: UserProfile, today: Date) {
    this._entry = entry;
    this._profile = profile;
    this._avatar = resolveAvatar([entry.body.username])[0];
    this._today = today;

  }
  //get taskTitle() { return this._taskTitle }
  get profile() { return this._profile }
  get entry() { return this._entry }
  get id() { return this._entry.id }
  get avatar() { return this._avatar }

  toPerson(): Person {
    return this._entry.body as Person;
  }
}

export { CustomerDescriptorImpl };
export type { };
