import { Backend, Store, Health } from './backend-types';

export class ServiceImpl implements Backend {
  private _store: Store;

  constructor(store: Store) {
    this._store = store;
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
}