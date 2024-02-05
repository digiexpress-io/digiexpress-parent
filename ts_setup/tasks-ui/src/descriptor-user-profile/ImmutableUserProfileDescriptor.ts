import { NotificationSetting, UserProfile, UserProfileDescriptor } from './profile-types';




export class ImmutableUserProfileDescriptor implements UserProfileDescriptor {
  private _entry: UserProfile;
  private _created: Date;
  private _updated: Date;
  private _notificationSettings: NotificationSetting[];

  constructor(entry: UserProfile) {
    this._entry = entry;
    this._created = new Date(this.entry.created);
    this._updated = new Date(this.entry.updated);
    this._notificationSettings = this.entry.notificationSettings;
  }
  get entry() { return this._entry }
  get id() { return this._entry.id }
  get notificationSettings() { return this._notificationSettings }
  get created(): Date { return this._created }
  get updated(): Date { return this._updated }
  get email(): string { return this._entry.details.email }
  get firstName(): string { return this._entry.details.firstName }
  get lasttName(): string { return this._entry.details.lastName }
  get displayName(): string { return this._entry.details.lastName + " " + this._entry.details.firstName }
}
