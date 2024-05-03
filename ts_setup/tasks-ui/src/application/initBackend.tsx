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

const getWs = () => {
  try {
    const url = getUrl()
    if(url.indexOf("http") === 0) {
      return "ws" + url.substring(4)
    }
    return "wss" + window.location.origin.substring(5);
  } catch (error) {
    return "";
  }
}

const baseUrl = getUrl();
const baseWs = getWs();

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
    'EVENTS': baseWs + "/q/digiexpress/api/",
  },
  performInitCheck: false,
  csrf: window._env_?.csrf,
  oidc: window._env_?.oidc,
  status: window._env_?.status,
});



export function initBackend() {
  return new Backend.BackendImpl(store);
}
