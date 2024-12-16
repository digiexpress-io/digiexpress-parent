import { SiteApi } from "@dxs-ts/gamut";

export function createSiteFetch(url: (string | undefined) = '/portal/site') {
  const fetchSiteGet: SiteApi.FetchSiteGET = async (locale: string) => {
    const response = await window.fetch(`${url}?locale=${locale}`, {
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

  const fetchFeedbackRatingPut: SiteApi.FetchFeedbackRatingPUT = async (body) => {
    const response = await window.fetch(`${url}/feedback`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      credentials: undefined,
      body: JSON.stringify(body),
    });
    return response;
  }

  return { fetchSiteGet, fetchFeedbackGet, fetchFeedbackRatingPut };
}
