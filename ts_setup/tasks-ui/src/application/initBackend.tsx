import React from 'react';
import Backend, { useBackend, Health } from 'descriptor-backend';
import { ImmutableAmStore, UserProfileAndOrg } from 'descriptor-access-mgmt';



interface Csrf { key: string, value: string }
declare global {
  interface Window {
    _env_: {
      url?: string,
      csrf?: Csrf,
      oidc?: string,
      status?: string,
    }
  }
}
const getUrl = () => {
  try {
    window.LOG_FACTORY.getLogger().debug(`application mode: ${process.env.REACT_APP_LOCAL_DEV_MODE}`);
    if (process.env.REACT_APP_LOCAL_DEV_MODE) {
      return "http://localhost:8080";
    }
    return "";
  } catch (error) {
    return "";
  }
}

const baseUrl = getUrl();

const store: Backend.Store = new Backend.BackendStoreImpl({
  urls: {
    'TASKS': baseUrl + "/q/digiexpress/api/",
    'TENANT': baseUrl + "/q/digiexpress/api/",
    'CRM': baseUrl + "/q/digiexpress/api/",
    'STENCIL': baseUrl + "/q/digiexpress/api/",
    'USER_PROFILE': baseUrl + "/q/digiexpress/api/",
    'WRENCH': baseUrl + "/q/digiexpress/api/",
    'CONFIG': baseUrl + "/q/digiexpress/api/",
    'HEALTH': baseUrl + "/q/digiexpress/api/",
    'SYS_CONFIG': baseUrl + "/q/digiexpress/api/",
    'PERMISSIONS': baseUrl + "/q/digiexpress/api/",
    'DIALOB': baseUrl + "/q/digiexpress/api/",
    'AVATAR': baseUrl + "/q/digiexpress/api/",
  },
  performInitCheck: false,
  csrf: window._env_?.csrf,
  oidc: window._env_?.oidc,
  status: window._env_?.status,
});



export function initBackend() {
  return new Backend.BackendImpl(store);
}


export function useHealth() {
  const backend = useBackend();
  const [health, setHealth] = React.useState<Health>();

  React.useEffect(() => {
    backend.health().then(health => {
      if (health.contentType === 'NO_CONNECTION') {

      } else if (health.contentType === 'BACKEND_NOT_FOUND') {

      }
      setHealth(health);
    })
      .catch(err => {
        console.error(err);
      });
  }, []);

  return { health };
}

export function useProfile() {
  const backend = useBackend();
  const [profile, setProfile] = React.useState<UserProfileAndOrg>();

  React.useEffect(() => {
    new ImmutableAmStore(backend.store).currentUserProfile()
      .then(setProfile)
      .catch(err => {
        console.error(err);
      });
  }, []);


  return { profile }
}


