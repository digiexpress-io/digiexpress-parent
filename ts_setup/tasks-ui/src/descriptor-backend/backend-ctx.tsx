import React from 'react';
import { Backend, Store, Health, BackendAccess, ForbiddenCallback } from './backend-types';
import { Forbidden } from './Forbidden';

export class BackendImpl implements Backend {
  private _store: Store;
  private _forbidden: ForbiddenCallback | undefined;

  constructor(store: Store, forbidden?: ForbiddenCallback | undefined) {
    this._store = store;
    this._forbidden = forbidden;
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

  withForbidden(handles: (access: BackendAccess) => void): Backend {
    const next = this._store.withForbidden(handles);
    return new BackendImpl(next, handles);
  }
}

export const BackendContext = React.createContext<Backend>({} as any);


export const BackendProvider: React.FC<{ children: React.ReactNode, backend: Backend}> = (props) => {
  const init = props.backend;
  const [access, setAccess] = React.useState<BackendAccess>();
  const contextValue = React.useMemo(() => init.withForbidden(setAccess), [init]);
  
  function handleAccessClose() {
    setAccess(undefined);
  }

  return (<BackendContext.Provider value={contextValue}>
    {props.children}
    <Forbidden access={access} onClose={handleAccessClose}/>
  </BackendContext.Provider>);
}

export const useBackend = () => {
  return React.useContext(BackendContext);
}