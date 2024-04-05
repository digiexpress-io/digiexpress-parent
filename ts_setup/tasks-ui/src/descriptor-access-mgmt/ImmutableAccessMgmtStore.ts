import { CreatePermission, CreateRole, Permission, AccessMgmtStore, PermissionUpdateCommand, Role, RoleUpdateCommand, CreatePrincipal, Principal } from './access-mgmt-types';


export interface AccessMgmtStoreConfig {
  fetch<T>(path: string, init: RequestInit & { notFound?: () => T, repoType: 'PERMISSIONS' }): Promise<T>;
}

export class ImmutableAccessMgmtStore implements AccessMgmtStore {
  private _store: AccessMgmtStoreConfig;

  constructor(store: AccessMgmtStoreConfig) {
    this._store = store;
  }

  withStore(store: AccessMgmtStoreConfig): ImmutableAccessMgmtStore {
    return new ImmutableAccessMgmtStore(store);
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

  async findAllPrincipals(): Promise<Principal[]> {
    return await this._store.fetch<Principal[]>(`principals`, { repoType: 'PERMISSIONS' });
  }

  async createPrincipal(command: CreatePrincipal): Promise<Principal> {
    return await this._store.fetch<Principal>(`principals`, {
      method: 'POST',
      body: JSON.stringify(command),
      repoType: 'PERMISSIONS'
    });
  }
}