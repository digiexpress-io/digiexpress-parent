import { useDialobDashboardFetch } from '../context';
import type { DialobAdminConfig } from '../types';

export const getHeaders = (config: DialobAdminConfig): Record<string, string> => {
  const headers: Record<string, string> = {
    Accept: 'application/json',
    'Content-Type': 'application/json; charset=utf-8'
  };
  if (config.csrf) {
    headers[config.csrf.key] = config.csrf.value;
  }
  return headers;
};

export const useFetchAuth = () => {
  const fetch = useDialobDashboardFetch();

  return async (
    url: string,
    init: RequestInit,
    config: DialobAdminConfig
  ): Promise<Response> => {
    if (!init.headers) {
      init.headers = getHeaders(config);
    }

    return fetch(url, {
      credentials: init.credentials ?? 'same-origin',
      ...init
    });
  };
};