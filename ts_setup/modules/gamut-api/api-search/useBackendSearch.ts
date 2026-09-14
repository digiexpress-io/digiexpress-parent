import React from 'react';

import { useConfig } from '../api-config';
import { useLocale } from '../api-locale';
import { SearchApi } from './search-types';


const DEBOUNCE_MS = 350;
const TIMEOUT_MS = 8000;
const LOADER_DELAY_MS = 250;

const UNAVAILABLE = 'unavailable';

function useLoader(inProgress: boolean, delayMs: number): boolean {
  const [loading, setLoading] = React.useState(false);

  React.useEffect(() => {
    if (!inProgress) {
      setLoading(false);
      return;
    }
    const id = window.setTimeout(() => setLoading(true), delayMs);
    return () => window.clearTimeout(id);
  }, [inProgress, delayMs]);

  return loading;
}

interface Answer {
  query: string;
  results: SearchApi.BackendSearchResult[] | undefined;
}

export function useBackendSearch(searchString: string | undefined): SearchApi.BackendSearchState {
  const { siteSearchFetch } = useConfig();
  const { locale } = useLocale();

  const [answered, setAnswered] = React.useState<Answer | undefined>(undefined);
  const [unavailable, setUnavailable] = React.useState(false);
  const [inProgress, setInProgress] = React.useState(false);

  const query = searchString?.trim() ?? '';

  React.useEffect(() => {
    setUnavailable(false);
    setAnswered(undefined);
    setInProgress(false);
  }, [locale, siteSearchFetch]);

  React.useEffect(() => {
    if (query.length === 0) {
      setAnswered(undefined);
      return;
    }
    if (unavailable) {
      return;
    }

    let cancelled = false;

    const controller = new AbortController();
    const timeout = window.setTimeout(() => controller.abort(), DEBOUNCE_MS + TIMEOUT_MS);
    const debounce = window.setTimeout(() => {
      if (cancelled) {
        return;
      }
      setInProgress(true);
      siteSearchFetch.fetchSiteSearchGet(query, locale, undefined, controller.signal)
        .then(async response => {
          if (response.status === 404 || response.status === 401 || response.status === 403) {
            return UNAVAILABLE;
          }
          if (!response.ok) {
            return undefined;
          }
          const body: SearchApi.BackendSearchResponse = await response.json();
          if (body.fallback || !Array.isArray(body.results)) {
            return undefined;
          }
          return body.results;
        })
        .catch(() => undefined)
        .then(next => {
          if (cancelled) {
            return;
          }
          window.clearTimeout(timeout);
          setInProgress(false);
          if (next === UNAVAILABLE) {
            setUnavailable(true);
            return;
          }
          setAnswered({ query, results: next });
        });
    }, DEBOUNCE_MS);

    return () => {
      cancelled = true;
      window.clearTimeout(debounce);
      window.clearTimeout(timeout);
      controller.abort();
      setInProgress(false);
    };
  }, [query, locale, siteSearchFetch, unavailable]);

  const status: SearchApi.BackendSearchStatus = unavailable
    ? 'unavailable'
    : (query.length === 0 || answered?.query === query) ? 'ready' : 'pending';

  const results = unavailable ? undefined : answered?.results;
  const loading = status === 'pending';
  const showLoader = useLoader(inProgress, LOADER_DELAY_MS);

  return React.useMemo(
    () => ({ status, results, query: answered?.query, loading, showLoader }),
    [status, results, answered, loading, showLoader]);
}
