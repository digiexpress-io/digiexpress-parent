import { SiteApi } from "../api-site";

export function createSiteFetch(url: (string | undefined) = '/portal/site') {
  const fetchSiteGet: SiteApi.FetchSiteGET = async (locale: string, cockpitId?: string) => {
    const tenant = cockpitId ? `&cockpitId=${cockpitId}` : '';
    const response = await window.fetch(`${url}?locale=${locale}${tenant}`, {
      method: 'GET',
      headers: undefined,
      credentials: undefined,
    });
    return response;
  }

  const fetchCockpitsGet: SiteApi.FetchCockpitsGET = async () => {
    const response = await window.fetch(`${url}/cockpits`, {
      method: 'GET',
      headers: undefined,
      credentials: undefined,
    });
    return response;
  }

  const fetchFeedbackGet: SiteApi.FetchSiteGET = async (locale: string) => {
    const response = await window.fetch(`${url}/feedback?locale=${locale}`, {
      method: 'GET',
      headers: undefined,
      credentials: undefined,
    });
    return response;
  }

  return { fetchSiteGet, fetchCockpitsGet, fetchFeedbackGet };
}
