import { Backend, Store, Health } from './backend-types';

import { TenantConfig } from 'client';

import { UserProfile, UserProfileAndOrg} from 'descriptor-user-profile'


import { mockOrg } from './client-mock';


export class ServiceImpl implements Backend {
  private _store: Store;

  constructor(store: Store) {
    this._store = store;
  }

  withTenantConfig(tenant: TenantConfig): ServiceImpl {
    return new ServiceImpl(this._store.withTenantConfig(tenant));
  }

  get store() { return this._store }
  get config() { return this._store.config; }
  async health(): Promise<Health> {
    try {
      await this._store.fetch<{}>('config/health', { repoType: 'HEALTH' });
      const result: Health = { contentType: 'OK' };
      return result;
    } catch (error) {
      console.error(error);
    }
    const result: Health = { contentType: 'BACKEND_NOT_FOUND' };
    return result;
  }

  async currentUserProfile(): Promise<UserProfileAndOrg> {
    const { today, org } = mockOrg;

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

      }>(`config/current-user`, { repoType: 'CONFIG' })


      return {
        userId: config.profile.id,
        am: {
          permissions: config.permissions.permissions,
          roles: Object.keys(org.roles)
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
}