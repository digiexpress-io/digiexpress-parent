import React from 'react';
import { Backend, Store, Health, BackendAccess, ForbiddenCallback } from './backend-types';
import { Forbidden } from './Forbidden';
import { ImmutableAmStore, UserProfileAndOrg } from 'descriptor-access-mgmt';
import { Loader } from './Loader';



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

export interface BackendContextType {
  backend: Backend;
  profile: UserProfileAndOrg;
  health: Health;
}

export const BackendContext = React.createContext<BackendContextType>({} as any);


const BackendProviderDelegate: React.FC<{ 
  children: React.ReactNode;
  backend: Backend;
  profile: UserProfileAndOrg;
  health: Health;
}> = (props) => {

  const init = props.backend;
  const [access, setAccess] = React.useState<BackendAccess>();
  const [profile, setProfile] = React.useState<UserProfileAndOrg>(props.profile);
  const [health, setHealth] = React.useState<Health>(props.health);
  const backend = React.useMemo(() => init.withForbidden(setAccess), [init]);


  function handleAccessClose() {
    setAccess(undefined);
  }
  const contextValue: BackendContextType = React.useMemo(() => ({
    backend, profile, health
  }), [backend, profile, health]);

  return (<BackendContext.Provider value={contextValue}>
    {props.children}
    <Forbidden access={access} onClose={handleAccessClose}/>
  </BackendContext.Provider>);
}


export const BackendProvider: React.FC<{ children: React.ReactNode, backend: Backend}> = ({ backend, children }) => {
  const [profile, setProfile] = React.useState<UserProfileAndOrg>();
  const [health, setHealth] = React.useState<Health>();


  React.useEffect(() => {
    new ImmutableAmStore(backend.store).currentUserProfile()
      .then(setProfile)
      .catch(err => {
        console.error(err);
      });
  }, []);

  React.useEffect(() => {
    backend.health().then(health => {
      if (health.contentType === 'NO_CONNECTION') {

      } else if (health.contentType === 'BACKEND_NOT_FOUND') {

      }
      setHealth(health);
    }).catch(err => {
        console.error(err);
      });
  }, []);

  return (<>
    <Loader health={health} profile={profile}/>
    {health && profile && <BackendProviderDelegate backend={backend} health={health} profile={profile}>{children}</BackendProviderDelegate>}
  </>);
}
export const useBackend = () => {
  return React.useContext(BackendContext).backend;
}

export const useProfile = () => {
  return React.useContext(BackendContext).profile;
}