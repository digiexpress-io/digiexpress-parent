import type { UiSettings, UpsertUiSettings, UserProfile, UserProfileAndOrg, UserProfileUpdateCommand } from './profile-types';
import type {
  Permission, Role, Principal,
  CreatePermission, CreateRole, CreatePrincipal,
  PermissionUpdateCommand, RoleUpdateCommand, PrincipalUpdateCommand,
} from './permission-types';

import type { TenantConfig } from './tenant-types';
import type { AmStore } from './am-store-types';


export interface UserProfileStoreConfig {
  fetch<T>(path: string, init: RequestInit & { notFound?: () => T, repoType: 'USER_PROFILE' | 'PERMISSIONS' }): Promise<T>;
}

export class ImmutableAmStore implements AmStore {
  private _store: UserProfileStoreConfig;

  constructor(store: UserProfileStoreConfig) {
    this._store = store;
  }

  withStore(store: UserProfileStoreConfig): ImmutableAmStore {
    return new ImmutableAmStore(store);
  }

  get store() { return this._store }

  async currentUserProfile(): Promise<UserProfileAndOrg> {
    try {
      const config = await this._store.fetch<{
        permissions: { 
          principal: Principal;
          permissions: string[];
        };
        user: {
          userId: string;
          email: string;
          givenName: string;
          familyName: string;
        };
        tenant: TenantConfig;
        profile: UserProfile

      }>(`config/current-user`, { repoType: 'USER_PROFILE' });

      return {
        userId: config.profile.id,
        am: {
          principal: config.permissions.principal,
          permissions: config.permissions.permissions,
          roles: []
        },
        all: {
          permissions: {},
          principals: {},
          roles: {}
        },
        user: config.profile,
        tenant: config.tenant,
        today: new Date(),
      };
    } catch (error) {
      console.error("PROFILE, failed to fetch", error);
      return {} as any;
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

  async updateUiSettings(commands: UpsertUiSettings): Promise<UserProfile> {
    return await this._store.fetch<UserProfile>(`config/current-user/ui-settings`, {
      method: 'PUT',
      body: JSON.stringify(commands),
      repoType: 'USER_PROFILE'
    });
  }
  async findUiSettings(settingsId: string): Promise<UiSettings | undefined> {
    return await this._store.fetch<UiSettings| undefined>(`config/current-user/ui-settings/${settingsId}`, {
      repoType: 'USER_PROFILE',
      notFound: () => undefined
    });
  }


  async createPermission(command: CreatePermission): Promise<Permission> {
    return await this._store.fetch<Permission>(`am/permissions`, {
      method: 'POST',
      body: JSON.stringify(command),
      repoType: 'PERMISSIONS'
    });
  }

  async getPermission(id: string): Promise<Permission> {
    return await this._store.fetch<Permission>(`am/permissions/${id}`, { repoType: 'PERMISSIONS' });
  }

  async findAllPermissions(): Promise<Permission[]> {
    return await this._store.fetch<Permission[]>(`am/permissions`, { repoType: 'PERMISSIONS' });
  }

  async updatePermission(id: string, commands: PermissionUpdateCommand[]): Promise<Permission> {
    return await this._store.fetch<Permission>(`am/permissions/${id}`, {
      method: 'PUT',
      body: JSON.stringify(commands),
      repoType: 'PERMISSIONS'
    });
  }

  async createRole(command: CreateRole): Promise<Role> {
    return await this._store.fetch<Role>(`am/roles`, {
      method: 'POST',
      body: JSON.stringify(command),
      repoType: 'PERMISSIONS'
    });
  }

  async getRole(id: string): Promise<Role> {
    return await this._store.fetch<Role>(`am/roles/${id}`, { repoType: 'PERMISSIONS' });
  }

  async findAllRoles(): Promise<Role[]> {
    return await this._store.fetch<Role[]>(`am/roles`, { repoType: 'PERMISSIONS' });
  }

  async updateRole(id: string, commands: RoleUpdateCommand[]): Promise<Role> {
    return await this._store.fetch<Role>(`am/roles/${id}`, {
      method: 'PUT',
      body: JSON.stringify(commands),
      repoType: 'PERMISSIONS'
    });
  }

  async findAllPrincipals(): Promise<Principal[]> {
    return await this._store.fetch<Principal[]>(`am/principals`, { repoType: 'PERMISSIONS' });
  }

  async updatePrincipal(id: string, commands: PrincipalUpdateCommand[]): Promise<Permission> {
    return await this._store.fetch<Permission>(`am/principals/${id}`, {
      method: 'PUT',
      body: JSON.stringify(commands),
      repoType: 'PERMISSIONS'
    });
  }


  async createPrincipal(command: CreatePrincipal): Promise<Principal> {
    return await this._store.fetch<Principal>(`am/principals`, {
      method: 'POST',
      body: JSON.stringify(command),
      repoType: 'PERMISSIONS'
    });
  }
}