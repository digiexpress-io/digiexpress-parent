import type { UserProfileStore, UserProfile, UserProfileUpdateCommand } from './profile-types';

export interface UserProfileStoreConfig {
  fetch<T>(path: string, init: RequestInit & { notFound?: () => T, repoType: 'USER_PROFILE' }): Promise<T>;
}

export class ImmutableUserProfileStore implements UserProfileStore {
  private _store: UserProfileStoreConfig;

  constructor(store: UserProfileStoreConfig) {
    this._store = store;
  }

  withStore(store: UserProfileStoreConfig): ImmutableUserProfileStore {
    return new ImmutableUserProfileStore(store);
  }

  get store() { return this._store }

  async getUserProfileById(id: string): Promise<UserProfile> {
    return await this._store.fetch<UserProfile>(`userprofiles/${id}`, { repoType: 'USER_PROFILE' });
  }
  async findAllUserProfiles(): Promise<UserProfile[]> {
    return await this._store.fetch<UserProfile[]>(`userprofiles`, { repoType: 'USER_PROFILE' });
  }
  async updateUserProfile(profileId: string, commands: UserProfileUpdateCommand<any>[]): Promise<UserProfile> {
    return await this._store.fetch<UserProfile>(`userprofiles/${profileId}`, {
      method: 'PUT',
      body: JSON.stringify(commands),
      repoType: 'USER_PROFILE'
    });
  }
}