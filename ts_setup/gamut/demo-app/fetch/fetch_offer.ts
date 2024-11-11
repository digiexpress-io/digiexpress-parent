import { OfferApi } from "@dxs-ts/gamut";

export function createOfferFetch(url: (string | undefined) = '/portal/secured/actions') {
  const fetchPost: OfferApi.CreateOfferFetchPOST = async (request: OfferApi.OfferRequest) => {

    const superTestForm = undefined//'5032fb6bbd7d5eed6daf53d0f87603d3';

    const id = superTestForm ?? request.productId;
    const locale = request.locale;
    const inputContextId = request.pageId;
    const inputParentContextId = request.parentPageId ?? "";

    const query = `${url}?id=${id}&locale=${locale}&inputContextId=${inputContextId}&inputParentContextId=${inputParentContextId}`;

    const response = await window.fetch(query, {
      method: 'GET',
      headers: undefined,
      credentials: undefined,
    });
    return response;
  }

  const fetchGet: OfferApi.GetOffersFetchGET = async () => {
    // await new Promise((res) => setTimeout(() => { }, 2000));
    const response = await window.fetch(url, {
      method: 'GET',
      headers: undefined,
      credentials: undefined,
    });
    return response;
  }


  const fetchDelete: OfferApi.CancelOfferFetchDELETE = async (offer) => {
    // await new Promise((res) => setTimeout(() => { }, 2000));
    const response = await window.fetch(`${url}/${offer.id}`, {
      method: 'DELETE',
      headers: undefined,
      credentials: undefined,
    });
    return response;
  }
  return { fetchGet, fetchPost, fetchDelete };
}
