import type { UserProfileStore, UserProfile, UserProfileAndOrg, UserProfileUpdateCommand } from './profile-types';

import { TenantConfig } from './tenant-config-types';


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

  async currentUserProfile(): Promise<UserProfileAndOrg> {
    try {
      const config = await this._store.fetch<{
        permissions: { permissions: string [] };
        user: {
          userId: string;
          email: string;
          givenName: string;
          familyName: string;
        };
        tenant: TenantConfig;
        profile: UserProfile

      }>(`config/current-user`, { repoType: 'USER_PROFILE' })
      return {
        userId: config.profile.id,
        am: {
          permissions: config.permissions.permissions,
          roles: []
        },
        user: config.profile,
        tenant: config.tenant,
        today: new Date(),
      };
    } catch (error) {
      console.error("PROFILE, failed to fetch", error);
      throw error;
    }
  }

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