import { OfferApi } from "@dxs-ts/gamut";

export function createOfferFetch(url: (string | undefined) = 'http://localhost:8080/portal/secured/actions') {
  const fetchPost: OfferApi.CreateOfferFetchPOST = async (request: OfferApi.OfferRequest) => {

    const superTestForm = '5032fb6bbd7d5eed6daf53d0f87603d3';

    const response = await window.fetch(url + "?id=" + (superTestForm ?? request.productId) + "&locale=" + request.locale, {
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
    const response = await window.fetch(url, {
      method: 'DELETE',
      headers: undefined,
      credentials: undefined,
    });
    return response;
  }
  return { fetchGet, fetchPost, fetchDelete };
}
