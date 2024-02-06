import { SysConfigStore, SysConfig, CreateSysConfig, SysConfigUpdateCommand } from "./sys-config-types";


export interface SysConfigStoreConfig {
  fetch<T>(path: string, init: RequestInit & { notFound?: () => T, repoType: 'SYS_CONFIG' }): Promise<T>;
}

export class ImmutableCustomerStore implements SysConfigStore {
  private _store: SysConfigStoreConfig;

  constructor(store: SysConfigStoreConfig) {
    this._store = store;
  }

  withStore(store: SysConfigStoreConfig): ImmutableCustomerStore {
    return new ImmutableCustomerStore(store);
  }

  get store() { return this._store }

  async findAllSysConfigs(): Promise<SysConfig[]> {
    return await this._store.fetch<SysConfig[]>(`sys-configs`, { repoType: 'SYS_CONFIG' });
  }
 
  async getOneSysConfig(sysConfigId: string) {
    return await this._store.fetch<SysConfig>(`sys-configs/${sysConfigId}`, { repoType: 'SYS_CONFIG' });
  }

  async createOneSysConfig(commands: CreateSysConfig): Promise<SysConfig> {
    return await this._store.fetch<SysConfig>(`sys-configs`, { 
      repoType: 'SYS_CONFIG',
      method: 'POST',
      body: JSON.stringify(commands),
    });
  }
  
  async updateOneSysConfig(sysConfigId: string, commands: SysConfigUpdateCommand[]): Promise<SysConfig>{
    return await this._store.fetch<SysConfig>(`sys-configs/${sysConfigId}`, { 
      repoType: 'SYS_CONFIG',
      method: 'PUT',
      body: JSON.stringify(commands),
    });
  }

  async deleteOneSysConfig(sysConfigId: string, commands: SysConfigUpdateCommand[]): Promise<SysConfig>{
    return await this._store.fetch<SysConfig>(`sys-configs/${sysConfigId}`, { 
      repoType: 'SYS_CONFIG',
      method: 'DELETE',
      body: JSON.stringify(commands),
    });
  }
}