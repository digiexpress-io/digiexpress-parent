import { SiteApi } from "@dxs-ts/gamut";

export function createSiteFetch(url: (string | undefined) = '/portal/site') {
  const fetchGet: SiteApi.FetchGET = async (locale: string) => {
    const response = await window.fetch(`${url}?locale=${locale}`, {
      method: 'GET',
      headers: undefined,
      credentials: undefined,
    });
    return response;
  }
  return { fetchGet };
}
