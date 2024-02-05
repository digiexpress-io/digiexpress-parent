import { Backend, Store, Health } from './backend-types';

import { TenantConfig } from 'client';

import { Tenant } from 'descriptor-dialob';
import { UserProfile, UserProfileAndOrg} from 'descriptor-user-profile'

import type { User, Org } from './org-types';
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
      // thats ok, fallback to dialob
    }

    try {
      const tenantsUp = this._store.fetch<Tenant[]>(`api/tenants`, { repoType: 'EXT_DIALOB' });
      const result: Health = { contentType: 'DIALOB_EXT' };
      return result;
    } catch (error) {
      // thats ok, nothing else to check
    }
    const result: Health = { contentType: 'BACKEND_NOT_FOUND' };
    return result;
  }
  async currentTenant(): Promise<TenantConfig> {
    const current = await this._store.fetch<TenantConfig>(`config/current-tenants`, { repoType: 'CONFIG' });
    const { id, archived, created, documentType, name, preferences, repoConfigs, status, transactions, updated, version } = current;

    return { id, archived, created, documentType, name, preferences, repoConfigs, status, transactions, updated, version };
  }

  async currentUserProfile(): Promise<UserProfileAndOrg> {
    const { today, org } = mockOrg;

    try {
      const user = await this._store.fetch<UserProfile>(`config/current-user-profile`, { repoType: 'CONFIG' })
      return {
        user,
        userId: user.id,
        today,
        roles: Object.keys(org.roles)
      };
    } catch (error) {
      console.error("PROFILE, failed to fetch", error);
      throw error;
    }
  }
  async org(): Promise<{ org: Org, user: User }> {
    const user = await this._store.fetch<UserProfile>(`config/current-user-profile`, { repoType: 'CONFIG' })
    return {
      org: mockOrg.org, user: {
        userId: user.id,
        activity: [],
        avatar: '',
        displayName: '',
        type: 'TASK_USER',
        userRoles: Object.keys(mockOrg.org.roles)
      }
    };
  }
}