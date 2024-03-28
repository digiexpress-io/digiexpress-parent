import { CreatePermission, Permission, PermissionStore, PermissionUpdateCommand } from './permission-types';


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

  async findPermissions(): Promise<Permission[]> {
    return await this._store.fetch<Permission[]>(`permissions`, { repoType: 'PERMISSIONS' });
  }

  async updatePermission(id: string, commands: PermissionUpdateCommand[]): Promise<Permission> {
    return await this._store.fetch<Permission>(`permissions/${id}`, {
      method: 'PUT',
      body: JSON.stringify(commands),
      repoType: 'PERMISSIONS'
    });
  }

}