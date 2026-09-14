import React from 'react';

import { useConfig } from '../api-config';
import { useLocale } from '../api-locale';
import { SearchApi } from './search-types';


const DEBOUNCE_MS = 350;
const TIMEOUT_MS = 8000;

const UNAVAILABLE = 'unavailable';

interface Answer {
  query: string;
  results: SearchApi.BackendSearchResult[] | undefined;
}

export function useBackendSearch(searchString: string | undefined): SearchApi.BackendSearchState {
  const { siteSearchFetch } = useConfig();
  const { locale } = useLocale();

  const [answered, setAnswered] = React.useState<Answer | undefined>(undefined);
  const [unavailable, setUnavailable] = React.useState(false);

  const query = searchString?.trim() ?? '';

  React.useEffect(() => {
    setUnavailable(false);
    setAnswered(undefined);
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
    };
  }, [query, locale, siteSearchFetch, unavailable]);

  const status: SearchApi.BackendSearchStatus = unavailable
    ? 'unavailable'
    : (query.length === 0 || answered?.query === query) ? 'ready' : 'pending';

  const results = unavailable ? undefined : answered?.results;

  return React.useMemo(
    () => ({ status, results, query: answered?.query, loading: status === 'pending' }),
    [status, results, answered]);
}
