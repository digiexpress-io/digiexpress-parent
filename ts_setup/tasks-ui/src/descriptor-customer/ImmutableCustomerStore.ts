import type { CustomerStore, Customer, CustomerId } from './customer-types';

export interface CustomerStoreConfig {
  fetch<T>(path: string, init: RequestInit & { notFound?: () => T, repoType: 'CRM' }): Promise<T>;
}

export class ImmutableCustomerStore implements CustomerStore {
  private _store: CustomerStoreConfig;

  constructor(store: CustomerStoreConfig) {
    this._store = store;
  }

  withStore(store: CustomerStoreConfig): CustomerStore {
    return new ImmutableCustomerStore(store);
  }

  get store() { return this._store }


  async getCustomer(id: CustomerId): Promise<Customer> {
    return await this._store.fetch<Customer>(`customers/${id}`, { repoType: 'CRM' });
  }
  async findCustomers(searchString: string): Promise<Customer[]> {
    return await this._store.fetch<Customer[]>(`customers/search?name=${encodeURIComponent(searchString)}`, { repoType: 'CRM' });
  }
}