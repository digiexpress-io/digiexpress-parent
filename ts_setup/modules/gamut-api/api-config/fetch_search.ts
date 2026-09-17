export type FetchSiteSearchGET = (
  query: string,
  locale: string,
  limit?: number,
  signal?: AbortSignal
) => Promise<Response>;


export function createSiteSearchFetch(url: (string | undefined) = '/portal/site/search') {

  const fetchSiteSearchGet: FetchSiteSearchGET = async (query, locale, limit, signal) => {
    const size = limit ? `&limit=${limit}` : '';
    const response = await window.fetch(`${url}?q=${encodeURIComponent(query)}&locale=${encodeURIComponent(locale)}${size}`, {
      method: 'GET',
      headers: undefined,
      credentials: undefined,
      signal,
    });
    return response;
  }

  return { fetchSiteSearchGet };
}
