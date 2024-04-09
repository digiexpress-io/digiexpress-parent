import React from 'react';
import { Backend, Store, Health } from './backend-types';

export class BackendImpl implements Backend {
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

export const BackendContext = React.createContext<Backend>({} as any);


export const BackendProvider: React.FC<{ children: React.ReactNode, backend: Backend}> = (props) => {
  const init = props.backend;
  const contextValue = React.useMemo(() => init, [init]);
  return (<BackendContext.Provider value={contextValue}>{props.children}</BackendContext.Provider>);
}

export const useBackend = () => {
  return React.useContext(BackendContext);
}