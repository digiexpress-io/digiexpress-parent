import { CreatePermission, CreateRole, Permission, PermissionStore, PermissionUpdateCommand, Role, RoleUpdateCommand } from './permission-types';


export interface PermissionStoreConfig {
  fetch<T>(path: string, init: RequestInit & { notFound?: () => T, repoType: 'PERMISSIONS' }): Promise<T>;
}

export class ImmutablePermissionStore implements PermissionStore {
  private _store: PermissionStoreConfig;

  constructor(store: PermissionStoreConfig) {
    this._store = store;
  }

  withStore(store: PermissionStoreConfig): ImmutablePermissionStore {
    return new ImmutablePermissionStore(store);
  }

  get store() { return this._store }

  async createPermission(command: CreatePermission): Promise<Permission> {
    return await this._store.fetch<Permission>(`permissions`, {
      method: 'POST',
      body: JSON.stringify(command),
      repoType: 'PERMISSIONS'
    });
  }

  async getPermission(id: string): Promise<Permission> {
    return await this._store.fetch<Permission>(`permissions/${id}`, { repoType: 'PERMISSIONS' });
  }

  async findAllPermissions(): Promise<Permission[]> {
    return await this._store.fetch<Permission[]>(`permissions`, { repoType: 'PERMISSIONS' });
  }

  async updatePermission(id: string, commands: PermissionUpdateCommand[]): Promise<Permission> {
    return await this._store.fetch<Permission>(`permissions/${id}`, {
      method: 'PUT',
      body: JSON.stringify(commands),
      repoType: 'PERMISSIONS'
    });
  }

  async createRole(command: CreateRole): Promise<Role> {
    return await this._store.fetch<Role>(`roles`, {
      method: 'POST',
      body: JSON.stringify(command),
      repoType: 'PERMISSIONS'
    });
  }

  async getRole(id: string): Promise<Role> {
    return await this._store.fetch<Role>(`roles/${id}`, { repoType: 'PERMISSIONS' });
  }

  async findAllRoles(): Promise<Role[]> {
    return await this._store.fetch<Role[]>(`roles`, { repoType: 'PERMISSIONS' });
  }

  async updateRole(id: string, commands: RoleUpdateCommand[]): Promise<Permission> {
    return await this._store.fetch<Role>(`roles/${id}`, {
      method: 'PUT',
      body: JSON.stringify(commands),
      repoType: 'PERMISSIONS'
    });
  }
}